package com.scamshield.app

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

// =========================================================
// SCAN RESULT
// =========================================================

data class ScanResult(
    val riskPercentage: Int,
    val level: String,
    val indicators: List<String>,
    val explanation: String,
    val links: List<String> = emptyList(),
    val phoneNumbers: List<String> = emptyList(),
    val emails: List<String> = emptyList()
)

// =========================================================
// SCAN HISTORY ITEM
// =========================================================

data class ScanHistoryItem(
    val id: Long,
    val message: String,
    val sender: String,
    val riskPercentage: Int,
    val level: String,
    val indicators: List<String>,
    val timestamp: Long
)

// =========================================================
// MESSAGE ANALYZER
// =========================================================

private val urlRegex =
    Regex("""(?i)\b(?:https?://|www\.)[^\s<>"']+""")

private val phoneRegex =
    Regex("""(?<!\d)(?:\+91[\s-]?)?[6-9]\d{9}(?!\d)""")

private val emailRegex =
    Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")

private val scamKeywords = listOf(

    // General scam
    "fraud", "scam", "scammer", "fake", "fraudulent",
    "suspicious activity", "unauthorized transaction", "account compromised",

    // Banking
    "bank details", "bank account", "account number", "account blocked",
    "account suspended", "account deactivated", "debit card", "credit card",
    "card number", "card blocked", "atm card", "net banking",

    // OTP / credentials
    "otp", "one time password", "verification code", "security code",
    "password", "pin", "upi pin", "cvv", "mpin", "login details",
    "credentials", "share otp", "send otp",

    // UPI / payment
    "upi", "upi payment", "payment", "pay now", "make payment", "send money",
    "transfer money", "money transfer", "payment failed", "payment pending",
    "payment refund", "collect request", "upi request", "scan qr",
    "scan qr code", "qr code", "cashback",

    // KYC
    "kyc", "kyc update", "kyc expired", "kyc verification", "complete kyc",
    "verify kyc", "verify your account", "account verification",
    "update your details", "pan card", "aadhaar",

    // Urgency / threats
    "urgent", "immediately", "act now", "within 24 hours", "within 12 hours",
    "within 1 hour", "expires today", "will expire", "last warning",
    "final warning", "do not ignore", "legal action", "police action",
    "arrest", "warrant",

    // Prize
    "lottery", "lottery winner", "you have won", "you won", "winner", "prize",
    "cash prize", "reward", "lucky winner", "congratulations",
    "claim your prize", "claim reward", "free gift", "gift voucher",

    // Refund
    "refund", "refund pending", "refund failed", "refund process",
    "customer care", "customer support", "verify refund", "claim refund",

    // Investment
    "investment", "invest now", "guaranteed return", "guaranteed profit",
    "double your money", "easy money", "quick profit", "stock tips",
    "trading profit", "crypto investment", "bitcoin", "forex",

    // Courier
    "courier", "parcel", "package", "delivery", "delivery failed",
    "parcel held", "parcel blocked", "customs", "customs fee",
    "customs charge", "shipping fee", "delivery charge",

    // Government / legal
    "income tax", "tax department", "cyber crime", "cybercrime", "court",
    "legal notice", "government notice",

    // Links / APK
    "click link", "click here", "click below", "open the link",
    "verify using this link", "verify through this link", "download apk",
    "install this app", "login here", "verify now"
)

/*
 * Whole-word matching, so "pin" no longer matches "shipping",
 * "otp" no longer matches "hotpot", "court" no longer matches "courtesy".
 */
private fun wordRegex(term: String): Regex =
    Regex("(?<![a-z0-9])" + Regex.escape(term) + "(?![a-z0-9])")

private val keywordRegexes: List<Pair<String, Regex>> =
    scamKeywords.map { it to wordRegex(it) }

fun analyzeMessage(message: String): ScanResult {

    val text = message.lowercase()

    // ---------------- EXTRACT DATA ----------------

    val links = urlRegex.findAll(message).map { it.value }.toList()
    val phoneNumbers = phoneRegex.findAll(message).map { it.value }.toList()
    val emails = emailRegex.findAll(message).map { it.value }.toList()

    val indicators = mutableListOf<String>()

    var risk = 0

    fun has(vararg terms: String): Boolean =
        terms.any { wordRegex(it).containsMatchIn(text) }

    // ---------------- KEYWORDS ----------------

    val matchedKeywords =
        keywordRegexes
            .filter { it.second.containsMatchIn(text) }
            .map { it.first }

    risk += matchedKeywords.size * 5

    matchedKeywords.forEach {
        indicators.add("Suspicious keyword: $it")
    }

    // ---------------- LINKS / PHONE / EMAIL ----------------

    if (links.isNotEmpty()) {
        risk += 15
        indicators.add("Suspicious link detected")
    }

    if (phoneNumbers.isNotEmpty()) {
        risk += 5
        indicators.add("Phone number detected")
    }

    if (emails.isNotEmpty()) {
        risk += 3
        indicators.add("Email address detected")
    }

    // ---------------- SPECIAL RISK CONDITIONS ----------------

    val asksForOtp =
        has("otp", "one time password", "verification code")

    val asksForMoney =
        has("send money", "transfer money", "pay now", "payment")

    val asksForBankDetails =
        has("bank details", "account number", "card number", "cvv", "upi pin")

    val hasUrgency =
        has("urgent", "immediately", "act now", "blocked") ||
                Regex("(?<![a-z0-9])expire").containsMatchIn(text) ||
                Regex("within \\d+ (?:hour|hours|minute|minutes|day|days)")
                    .containsMatchIn(text)

    val hasKyc =
        has("kyc", "verify your account")

    val hasPaymentLanguage =
        has("payment", "upi", "pay now", "transfer")

    val hasPrizeLanguage =
        has("lottery", "prize", "winner", "reward", "cashback")

    if (asksForOtp) {
        risk += 20
        indicators.add("OTP or verification code requested")
    }

    if (asksForMoney) {
        risk += 20
        indicators.add("Money/payment request detected")
    }

    if (asksForBankDetails) {
        risk += 20
        indicators.add("Banking/card information requested")
    }

    if (hasUrgency) {
        risk += 15
        indicators.add("Urgency or pressure detected")
    }

    if (hasKyc) {
        risk += 15
        indicators.add("KYC verification request detected")
    }

    if (hasPaymentLanguage) {
        risk += 10
        indicators.add("Payment/UPI language detected")
    }

    if (hasPrizeLanguage) {
        risk += 15
        indicators.add("Prize/reward language detected")
    }

    // ---------------- LIMIT + LEVEL ----------------

    risk = risk.coerceIn(0, 100)

    val level =
        when {
            risk >= 70 -> "HIGH"
            risk >= 40 -> "MEDIUM"
            else -> "LOW"
        }

    val explanation =
        when (level) {

            "HIGH" ->
                "Dangerous message detected. Do not click links, send money, or share OTPs, PINs, passwords, CVV or banking details."

            "MEDIUM" ->
                "This message contains suspicious indicators. Verify the sender independently before clicking links or making payments."

            else ->
                "No major scam indicators were detected. Still verify unexpected messages before taking action."
        }

    return ScanResult(
        riskPercentage = risk,
        level = level,
        indicators = indicators.distinct(),
        explanation = explanation,
        links = links,
        phoneNumbers = phoneNumbers,
        emails = emails
    )
}

// =========================================================
// REAL LOCAL STORAGE
// =========================================================

class ScanStorage(
    context: Context
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(
            "scamshield_data",
            Context.MODE_PRIVATE
        )

    private val legacyHistoryKey = "scan_history"
    private val historyKey = "scan_history_json"

    companion object {
        // One lock for every ScanStorage instance, because the SMS
        // receiver and the UI can save scans at the same time.
        private val lock = Any()
        private const val MAX_HISTORY = 100
    }

    // =========================================================
    // SAVE SCAN
    // =========================================================

    fun saveScan(
        message: String,
        result: ScanResult,
        sender: String = ""
    ) {

        synchronized(lock) {

            addToHistory(
                message = message,
                result = result,
                sender = sender
            )

            val total = getTotalScans() + 1

            val safe =
                getSafeScans() + if (result.riskPercentage < 40) 1 else 0

            val threats =
                getThreatScans() + if (result.riskPercentage >= 40) 1 else 0

            val today =
                SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    .format(Date())

            val todayScans =
                preferences.getInt("day_$today", 0) + 1

            preferences.edit()
                .putInt("total_scans", total)
                .putInt("safe_scans", safe)
                .putInt("threat_scans", threats)
                .putInt("day_$today", todayScans)
                .putString("last_message", message)
                .putInt("last_risk", result.riskPercentage)
                .putString("last_level", result.level)
                .putString("last_sender", sender)
                .apply()

            // Remember suspicious SMS senders
            if (result.riskPercentage >= 40 && sender.isNotBlank()) {
                saveSuspiciousSender(sender)
            }
        }
    }

    // =========================================================
    // RECORD A SAFE SCAN (only counters, the message is not stored)
    // =========================================================

    fun recordSafeScan() {

        synchronized(lock) {

            val today =
                SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    .format(Date())

            preferences.edit()
                .putInt("total_scans", getTotalScans() + 1)
                .putInt("safe_scans", getSafeScans() + 1)
                .putInt("day_$today", preferences.getInt("day_$today", 0) + 1)
                .apply()
        }
    }

    // =========================================================
    // SCAN HISTORY (JSON, with fallback to the old format)
    // =========================================================

    private fun itemToJson(item: ScanHistoryItem): JSONObject =
        JSONObject().apply {
            put("id", item.id)
            put("message", item.message)
            put("sender", item.sender)
            put("risk", item.riskPercentage)
            put("level", item.level)
            put("indicators", JSONArray(item.indicators))
            put("timestamp", item.timestamp)
        }

    private fun itemFromJson(obj: JSONObject): ScanHistoryItem {

        val indicatorArray = obj.optJSONArray("indicators") ?: JSONArray()

        return ScanHistoryItem(
            id = obj.getLong("id"),
            message = obj.optString("message", ""),
            sender = obj.optString("sender", ""),
            riskPercentage = obj.optInt("risk", 0),
            level = obj.optString("level", "LOW"),
            indicators = List(indicatorArray.length()) {
                indicatorArray.getString(it)
            },
            timestamp = obj.optLong("timestamp", 0L)
        )
    }

    // Reads history saved by the previous version of the app.
    private fun decodeLegacyItem(value: String): ScanHistoryItem? {

        val parts = value.split("|", limit = 7)

        if (parts.size != 7) {
            return null
        }

        return try {

            ScanHistoryItem(
                id = parts[0].toLong(),
                message = parts[1],
                sender = parts[2],
                riskPercentage = parts[3].toInt(),
                level = parts[4],
                indicators =
                    if (parts[5].isBlank()) emptyList()
                    else parts[5].split(";;"),
                timestamp = parts[6].toLong()
            )

        } catch (_: Exception) {
            null
        }
    }

    private fun readHistory(): List<ScanHistoryItem> {

        val json = preferences.getString(historyKey, null)

        if (json != null) {

            return try {

                val array = JSONArray(json)

                List(array.length()) {
                    itemFromJson(array.getJSONObject(it))
                }

            } catch (_: Exception) {
                emptyList()
            }
        }

        val legacy = preferences.getString(legacyHistoryKey, "") ?: ""

        if (legacy.isBlank()) {
            return emptyList()
        }

        return legacy
            .split("|||")
            .mapNotNull { decodeLegacyItem(it) }
    }

    private fun writeHistory(items: List<ScanHistoryItem>) {

        val array = JSONArray()

        items.forEach { array.put(itemToJson(it)) }

        preferences.edit()
            .putString(historyKey, array.toString())
            .remove(legacyHistoryKey)
            .apply()
    }

    fun addToHistory(
        message: String,
        result: ScanResult,
        sender: String = ""
    ) {

        synchronized(lock) {

            val history = readHistory().toMutableList()

            // Unique id, even when two scans arrive in the same millisecond
            // (the history list uses the id as its key).
            val now = System.currentTimeMillis()

            val id =
                maxOf(
                    now,
                    (history.maxOfOrNull { it.id } ?: 0L) + 1L
                )

            history.add(
                0,
                ScanHistoryItem(
                    id = id,
                    message = message,
                    sender = sender,
                    riskPercentage = result.riskPercentage,
                    level = result.level,
                    indicators = result.indicators,
                    timestamp = now
                )
            )

            writeHistory(history.take(MAX_HISTORY))
        }
    }

    fun getScanHistory(): List<ScanHistoryItem> =
        synchronized(lock) {
            readHistory().sortedByDescending { it.timestamp }
        }

    fun clearScanHistory() {

        synchronized(lock) {
            preferences.edit()
                .remove(historyKey)
                .remove(legacyHistoryKey)
                .apply()
        }
    }

    // =========================================================
    // SMS SENDER NORMALIZATION
    // =========================================================

    private fun normalizeSender(sender: String): String {

        val cleaned = sender.trim().lowercase()

        val digits = cleaned.filter { it.isDigit() }

        return if (digits.length >= 10) {
            "phone_${digits.takeLast(10)}"
        } else {
            "sender_" + cleaned.replace("[^a-z0-9]".toRegex(), "_")
        }
    }

    // =========================================================
    // SUSPICIOUS SMS SENDERS
    // =========================================================

    private fun saveSuspiciousSender(sender: String) {

        val key = normalizeSender(sender)

        if (key.isBlank()) {
            return
        }

        val count = preferences.getInt("suspicious_$key", 0) + 1

        preferences.edit()
            .putInt("suspicious_$key", count)
            .apply()
    }

    fun isKnownSuspiciousSender(sender: String): Boolean {

        if (sender.isBlank()) {
            return false
        }

        return preferences.getInt(
            "suspicious_${normalizeSender(sender)}",
            0
        ) > 0
    }

    fun getSuspiciousSenderCount(sender: String): Int {

        if (sender.isBlank()) {
            return 0
        }

        return preferences.getInt(
            "suspicious_${normalizeSender(sender)}",
            0
        )
    }

    // =========================================================
    // CALL PROTECTION
    // =========================================================

    fun normalizePhoneNumber(number: String): String {

        val digits = number.filter { it.isDigit() }

        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    fun reportSuspiciousCaller(phoneNumber: String) {

        val normalized = normalizePhoneNumber(phoneNumber)

        if (normalized.isBlank()) {
            return
        }

        preferences.edit()
            .putInt(
                "caller_$normalized",
                getSuspiciousCallerCount(normalized) + 1
            )
            .apply()
    }

    fun getSuspiciousCallerCount(phoneNumber: String): Int =
        preferences.getInt(
            "caller_${normalizePhoneNumber(phoneNumber)}",
            0
        )

    fun isSuspiciousCaller(phoneNumber: String): Boolean =
        getSuspiciousCallerCount(phoneNumber) > 0

    // =========================================================
    // DASHBOARD DATA
    // =========================================================

    fun getTotalScans(): Int =
        preferences.getInt("total_scans", 0)

    fun getSafeScans(): Int =
        preferences.getInt("safe_scans", 0)

    fun getThreatScans(): Int =
        preferences.getInt("threat_scans", 0)

    // =========================================================
    // SECURITY SCORE
    // =========================================================

    fun getSecurityScore(): Int {

        val total = getTotalScans()

        if (total == 0) {
            return 100
        }

        return (getSafeScans().toFloat() / total.toFloat() * 100)
            .toInt()
            .coerceIn(0, 100)
    }

    // =========================================================
    // LAST SCAN
    // =========================================================

    fun getLastRisk(): Int =
        preferences.getInt("last_risk", 0)

    fun getLastLevel(): String =
        preferences.getString("last_level", "") ?: ""

    fun getLastMessage(): String =
        preferences.getString("last_message", "") ?: ""

    fun getLastSender(): String =
        preferences.getString("last_sender", "") ?: ""

    // =========================================================
    // LAST 7 DAYS (oldest first, today is the last value)
    // =========================================================

    fun getLast7Days(): List<Int> {

        val values = mutableListOf<Int>()

        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        for (i in 6 downTo 0) {

            val calendar = Calendar.getInstance()

            calendar.add(Calendar.DAY_OF_YEAR, -i)

            values.add(
                preferences.getInt(
                    "day_${format.format(calendar.time)}",
                    0
                )
            )
        }


        return values
    }
}