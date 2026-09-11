package com.scamshield.app

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


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
// MESSAGE ANALYZER
// =========================================================

fun analyzeMessage(message: String): ScanResult {

    val text = message.lowercase()

    // =========================================================
    // REGEX
    // =========================================================

    val urlRegex =
        Regex("""(?i)\b(?:https?://|www\.)[^\s<>"']+""")

    val phoneRegex =
        Regex("""(?<!\d)(?:\+91[\s-]?)?[6-9]\d{9}(?!\d)""")

    val emailRegex =
        Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")


    // =========================================================
    // EXTRACT DATA
    // =========================================================

    val links =
        urlRegex.findAll(message)
            .map { it.value }
            .toList()

    val phoneNumbers =
        phoneRegex.findAll(message)
            .map { it.value }
            .toList()

    val emails =
        emailRegex.findAll(message)
            .map { it.value }
            .toList()

    val indicators =
        mutableListOf<String>()

    var risk = 0


    // =========================================================
    // SCAM KEYWORDS
    // =========================================================

    val scamKeywords = listOf(

        // General scam
        "fraud",
        "scam",
        "scammer",
        "fake",
        "fraudulent",
        "suspicious activity",
        "unauthorized transaction",
        "account compromised",

        // Banking
        "bank details",
        "bank account",
        "account number",
        "account blocked",
        "account suspended",
        "account deactivated",
        "debit card",
        "credit card",
        "card number",
        "card blocked",
        "atm card",
        "net banking",

        // OTP / credentials
        "otp",
        "one time password",
        "verification code",
        "security code",
        "password",
        "pin",
        "upi pin",
        "cvv",
        "mpin",
        "login details",
        "credentials",
        "share otp",
        "send otp",

        // UPI / payment
        "upi",
        "upi payment",
        "payment",
        "pay now",
        "make payment",
        "send money",
        "transfer money",
        "money transfer",
        "payment failed",
        "payment pending",
        "payment refund",
        "collect request",
        "upi request",
        "scan qr",
        "scan qr code",
        "qr code",
        "cashback",

        // KYC
        "kyc",
        "kyc update",
        "kyc expired",
        "kyc verification",
        "complete kyc",
        "verify kyc",
        "verify your account",
        "account verification",
        "update your details",
        "pan card",
        "aadhaar",

        // Urgency / threats
        "urgent",
        "immediately",
        "act now",
        "within 24 hours",
        "within 12 hours",
        "within 1 hour",
        "expires today",
        "will expire",
        "last warning",
        "final warning",
        "do not ignore",
        "legal action",
        "police action",
        "arrest",
        "warrant",

        // Prize
        "lottery",
        "lottery winner",
        "you have won",
        "you won",
        "winner",
        "prize",
        "cash prize",
        "reward",
        "lucky winner",
        "congratulations",
        "claim your prize",
        "claim reward",
        "free gift",
        "gift voucher",

        // Refund
        "refund",
        "refund pending",
        "refund failed",
        "refund process",
        "customer care",
        "customer support",
        "verify refund",
        "claim refund",

        // Investment
        "investment",
        "invest now",
        "guaranteed return",
        "guaranteed profit",
        "double your money",
        "easy money",
        "quick profit",
        "stock tips",
        "trading profit",
        "crypto investment",
        "bitcoin",
        "forex",

        // Courier
        "courier",
        "parcel",
        "package",
        "delivery",
        "delivery failed",
        "parcel held",
        "parcel blocked",
        "customs",
        "customs fee",
        "customs charge",
        "shipping fee",
        "delivery charge",

        // Government / legal
        "income tax",
        "tax department",
        "cyber crime",
        "cybercrime",
        "court",
        "legal notice",
        "government notice",

        // Links / APK
        "click link",
        "click here",
        "click below",
        "open the link",
        "verify using this link",
        "verify through this link",
        "download apk",
        "install this app",
        "login here",
        "verify now"
    )


    // =========================================================
    // MATCH KEYWORDS
    // =========================================================

    val matchedKeywords =
        scamKeywords.filter {
            text.contains(it)
        }

    risk += matchedKeywords.size * 5

    matchedKeywords.forEach {
        indicators.add(
            "Suspicious keyword: $it"
        )
    }


    // =========================================================
    // LINKS
    // =========================================================

    if (links.isNotEmpty()) {

        risk += 15

        indicators.add(
            "Suspicious link detected"
        )
    }


    // =========================================================
    // PHONE
    // =========================================================

    if (phoneNumbers.isNotEmpty()) {

        risk += 5

        indicators.add(
            "Phone number detected"
        )
    }


    // =========================================================
    // EMAIL
    // =========================================================

    if (emails.isNotEmpty()) {

        risk += 3

        indicators.add(
            "Email address detected"
        )
    }


    // =========================================================
    // SPECIAL RISK CONDITIONS
    // =========================================================

    val asksForOtp =
        text.contains("otp") ||
                text.contains("one time password") ||
                text.contains("verification code")

    val asksForMoney =
        text.contains("send money") ||
                text.contains("transfer money") ||
                text.contains("pay now") ||
                text.contains("payment")

    val asksForBankDetails =
        text.contains("bank details") ||
                text.contains("account number") ||
                text.contains("card number") ||
                text.contains("cvv") ||
                text.contains("upi pin")

    val hasUrgency =
        text.contains("urgent") ||
                text.contains("immediately") ||
                text.contains("act now") ||
                text.contains("within") ||
                text.contains("expire") ||
                text.contains("blocked")

    val hasKyc =
        text.contains("kyc") ||
                text.contains("verify your account")

    val hasPaymentLanguage =
        text.contains("payment") ||
                text.contains("upi") ||
                text.contains("pay now") ||
                text.contains("transfer")

    val hasPrizeLanguage =
        text.contains("lottery") ||
                text.contains("prize") ||
                text.contains("winner") ||
                text.contains("reward") ||
                text.contains("cashback")


    // =========================================================
    // OTP
    // =========================================================

    if (asksForOtp) {

        risk += 20

        indicators.add(
            "OTP or verification code requested"
        )
    }


    // =========================================================
    // MONEY
    // =========================================================

    if (asksForMoney) {

        risk += 20

        indicators.add(
            "Money/payment request detected"
        )
    }


    // =========================================================
    // BANK DETAILS
    // =========================================================

    if (asksForBankDetails) {

        risk += 20

        indicators.add(
            "Banking/card information requested"
        )
    }


    // =========================================================
    // URGENCY
    // =========================================================

    if (hasUrgency) {

        risk += 15

        indicators.add(
            "Urgency or pressure detected"
        )
    }


    // =========================================================
    // KYC
    // =========================================================

    if (hasKyc) {

        risk += 15

        indicators.add(
            "KYC verification request detected"
        )
    }


    // =========================================================
    // PAYMENT
    // =========================================================

    if (hasPaymentLanguage) {

        risk += 10

        indicators.add(
            "Payment/UPI language detected"
        )
    }


    // =========================================================
    // PRIZE
    // =========================================================

    if (hasPrizeLanguage) {

        risk += 15

        indicators.add(
            "Prize/reward language detected"
        )
    }


    // =========================================================
    // LIMIT RISK
    // =========================================================

    risk =
        risk.coerceIn(
            0,
            100
        )


    // =========================================================
    // RISK LEVEL
    // =========================================================

    val level =
        when {

            risk >= 70 ->
                "HIGH"

            risk >= 40 ->
                "MEDIUM"

            else ->
                "LOW"
        }


    // =========================================================
    // EXPLANATION
    // =========================================================

    val explanation =
        when (level) {

            "HIGH" ->
                "Dangerous message detected. Do not click links, send money, or share OTPs, PINs, passwords, CVV or banking details."

            "MEDIUM" ->
                "This message contains suspicious indicators. Verify the sender independently before clicking links or making payments."

            else ->
                "No major scam indicators were detected. Still verify unexpected messages before taking action."
        }


    // =========================================================
    // RETURN RESULT
    // =========================================================

    return ScanResult(

        riskPercentage = risk,

        level = level,

        indicators =
            indicators.distinct(),

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


    // =========================================================
    // SAVE SCAN
    // =========================================================

    fun saveScan(
        message: String,
        result: ScanResult,
        sender: String = ""
    ) {

        val total =
            getTotalScans() + 1

        val safe =
            getSafeScans() +
                    if (
                        result.riskPercentage < 40
                    ) {
                        1
                    } else {
                        0
                    }

        val threats =
            getThreatScans() +
                    if (
                        result.riskPercentage >= 40
                    ) {
                        1
                    } else {
                        0
                    }

        val today =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())

        val todayScans =
            preferences.getInt(
                "day_$today",
                0
            ) + 1


        preferences.edit()

            .putInt(
                "total_scans",
                total
            )

            .putInt(
                "safe_scans",
                safe
            )

            .putInt(
                "threat_scans",
                threats
            )

            .putInt(
                "day_$today",
                todayScans
            )

            .putString(
                "last_message",
                message
            )

            .putInt(
                "last_risk",
                result.riskPercentage
            )

            .putString(
                "last_level",
                result.level
            )

            .putString(
                "last_sender",
                sender
            )

            .apply()


        // Remember suspicious SMS senders
        if (
            result.riskPercentage >= 40 &&
            sender.isNotBlank()
        ) {

            saveSuspiciousSender(
                sender
            )
        }
    }


    // =========================================================
    // SMS SENDER NORMALIZATION
    // =========================================================

    private fun normalizeSender(
        sender: String
    ): String {

        val cleaned =
            sender.trim().lowercase()

        val digits =
            cleaned.filter {
                it.isDigit()
            }

        return if (
            digits.length >= 10
        ) {

            "phone_${digits.takeLast(10)}"

        } else {

            "sender_" +
                    cleaned.replace(
                        "[^a-z0-9]".toRegex(),
                        "_"
                    )
        }
    }


    // =========================================================
    // SAVE SUSPICIOUS SMS SENDER
    // =========================================================

    private fun saveSuspiciousSender(
        sender: String
    ) {

        val key =
            normalizeSender(sender)

        if (key.isBlank()) {
            return
        }

        val count =
            preferences.getInt(
                "suspicious_$key",
                0
            ) + 1

        preferences.edit()
            .putInt(
                "suspicious_$key",
                count
            )
            .apply()
    }


    // =========================================================
    // CHECK KNOWN SUSPICIOUS SMS SENDER
    // =========================================================

    fun isKnownSuspiciousSender(
        sender: String
    ): Boolean {

        if (sender.isBlank()) {
            return false
        }

        val key =
            normalizeSender(sender)

        return preferences.getInt(
            "suspicious_$key",
            0
        ) > 0
    }


    // =========================================================
    // GET SUSPICIOUS SMS COUNT
    // =========================================================

    fun getSuspiciousSenderCount(
        sender: String
    ): Int {

        if (sender.isBlank()) {
            return 0
        }

        val key =
            normalizeSender(sender)

        return preferences.getInt(
            "suspicious_$key",
            0
        )
    }


    // =========================================================
    // CALL PROTECTION
    // =========================================================

    fun normalizePhoneNumber(
        number: String
    ): String {

        val digits =
            number.filter {
                it.isDigit()
            }

        return if (
            digits.length >= 10
        ) {

            digits.takeLast(10)

        } else {

            digits
        }
    }


    // =========================================================
    // REPORT SUSPICIOUS CALLER
    // =========================================================

    fun reportSuspiciousCaller(
        phoneNumber: String
    ) {

        val normalized =
            normalizePhoneNumber(
                phoneNumber
            )

        if (normalized.isBlank()) {
            return
        }

        val currentCount =
            getSuspiciousCallerCount(
                normalized
            )

        preferences.edit()
            .putInt(
                "caller_$normalized",
                currentCount + 1
            )
            .apply()
    }


    // =========================================================
    // GET SUSPICIOUS CALLER COUNT
    // =========================================================

    fun getSuspiciousCallerCount(
        phoneNumber: String
    ): Int {

        val normalized =
            normalizePhoneNumber(
                phoneNumber
            )

        return preferences.getInt(
            "caller_$normalized",
            0
        )
    }


    // =========================================================
    // CHECK SUSPICIOUS CALLER
    // =========================================================

    fun isSuspiciousCaller(
        phoneNumber: String
    ): Boolean {

        return getSuspiciousCallerCount(
            phoneNumber
        ) > 0
    }


    // =========================================================
    // DASHBOARD DATA
    // =========================================================

    fun getTotalScans(): Int =
        preferences.getInt(
            "total_scans",
            0
        )


    fun getSafeScans(): Int =
        preferences.getInt(
            "safe_scans",
            0
        )


    fun getThreatScans(): Int =
        preferences.getInt(
            "threat_scans",
            0
        )


    // =========================================================
    // SECURITY SCORE
    // =========================================================

    fun getSecurityScore(): Int {

        val total =
            getTotalScans()

        if (total == 0) {
            return 100
        }

        return (
                getSafeScans()
                    .toFloat() /
                        total.toFloat() *
                        100
                )
            .toInt()
            .coerceIn(
                0,
                100
            )
    }


    // =========================================================
    // LAST SCAN
    // =========================================================

    fun getLastRisk(): Int =
        preferences.getInt(
            "last_risk",
            0
        )


    fun getLastLevel(): String =
        preferences.getString(
            "last_level",
            ""
        ) ?: ""


    fun getLastMessage(): String =
        preferences.getString(
            "last_message",
            ""
        ) ?: ""


    fun getLastSender(): String =
        preferences.getString(
            "last_sender",
            ""
        ) ?: ""


    // =========================================================
    // LAST 7 DAYS
    // =========================================================

    fun getLast7Days(): List<Int> {

        val calendar =
            Calendar.getInstance()

        val values =
            mutableListOf<Int>()

        for (i in 6 downTo 0) {

            calendar.time =
                Date()

            calendar.add(
                Calendar.DAY_OF_YEAR,
                -i
            )

            val date =
                SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).format(
                    calendar.time
                )

            values.add(
                preferences.getInt(
                    "day_$date",
                    0
                )
            )
        }

        return values
    }
}