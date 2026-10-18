package com.scamshield.app

import java.net.URI

// =========================================================
// INPUT SOURCE
// SMS / Message / QR / URL / Screenshot
// =========================================================

enum class InputSource {
    SMS,
    MESSAGE,
    QR,
    URL,
    SCREENSHOT
}

// =========================================================
// CATEGORY
// =========================================================

enum class ScamCategory(val title: String) {

    OTP_THEFT(
        "OTP / credential theft"
    ),

    BANK_KYC(
        "Bank or KYC scam"
    ),

    UPI_PAYMENT(
        "UPI / payment scam"
    ),

    PRIZE(
        "Prize or lottery scam"
    ),

    COURIER(
        "Courier or parcel scam"
    ),

    INVESTMENT(
        "Investment scam"
    ),

    GOVERNMENT(
        "Fake government or legal threat"
    ),

    PHISHING_LINK(
        "Phishing or unsafe link"
    ),

    GENERAL(
        "Suspicious message"
    ),

    NONE(
        "No scam detected"
    )
}

// =========================================================
// FINAL RESULT
// Risk + Category + Reasons + Actions
// =========================================================

data class ScamShieldResult(

    val source: InputSource,

    val riskPercentage: Int,

    // HIGH / MEDIUM / LOW
    val level: String,

    val category: ScamCategory,

    val reasons: List<String>,

    val actions: List<String>,

    val explanation: String,

    val links: List<String>,

    val phoneNumbers: List<String>,

    val emails: List<String>

) {

    /*
     * Converts ScamShieldResult into the existing ScanResult
     * used by ScanStorage.
     */
    fun toScanResult() =
        ScanResult(
            riskPercentage = riskPercentage,
            level = level,
            indicators = reasons,
            explanation = explanation,
            links = links,
            phoneNumbers = phoneNumbers,
            emails = emails
        )
}

// =========================================================
// RISK ENGINE
// =========================================================

object RiskEngine {

    // ---------------------------------------------------------
    // URL SHORTENERS
    // ---------------------------------------------------------

    private val shorteners =
        setOf(
            "bit.ly",
            "tinyurl.com",
            "t.co",
            "cutt.ly",
            "rb.gy",
            "is.gd",
            "goo.gl",
            "shorturl.at",
            "ow.ly",
            "buff.ly"
        )

    // ---------------------------------------------------------
    // SUSPICIOUS TLDs
    // ---------------------------------------------------------

    private val riskyTlds =
        setOf(
            "xyz",
            "top",
            "click",
            "link",
            "icu",
            "live",
            "site",
            "online",
            "buzz",
            "monster",
            "cfd",
            "sbs"
        )

    // ---------------------------------------------------------
    // BRAND → OFFICIAL DOMAINS
    // ---------------------------------------------------------

    private val brands =
        mapOf(

            "hdfc" to setOf(
                "hdfcbank.com"
            ),

            "sbi" to setOf(
                "sbi.co.in",
                "onlinesbi.sbi"
            ),

            "icici" to setOf(
                "icicibank.com"
            ),

            "axis" to setOf(
                "axisbank.com"
            ),

            "paytm" to setOf(
                "paytm.com"
            ),

            "phonepe" to setOf(
                "phonepe.com"
            ),

            "amazon" to setOf(
                "amazon.in",
                "amazon.com"
            ),

            "flipkart" to setOf(
                "flipkart.com"
            ),

            "kyc" to emptySet()
        )

    // =========================================================
    // TEXT MATCHING
    // =========================================================

    private fun has(
        text: String,
        vararg terms: String
    ): Boolean {

        return terms.any {

            Regex(
                "(?<![a-z0-9])" +
                        Regex.escape(it) +
                        "(?![a-z0-9])"
            ).containsMatchIn(text)
        }
    }

    // =========================================================
    // LOCAL URL CHECKS
    // =========================================================

    private fun analyzeUrls(
        links: List<String>
    ): Pair<Int, List<String>> {

        var risk = 0

        val reasons =
            mutableListOf<String>()

        for (link in links) {

            val normalized =
                if (
                    link.startsWith(
                        "http",
                        ignoreCase = true
                    )
                ) {
                    link
                } else {
                    "http://$link"
                }

            val host =
                try {

                    URI(normalized)
                        .host
                        ?.lowercase()

                } catch (_: Exception) {

                    null
                }
                    ?: continue

            val tld =
                host.substringAfterLast('.')

            val baseDomain =
                host
                    .split('.')
                    .takeLast(2)
                    .joinToString(".")

            // -------------------------------------------------
            // URL SHORTENER
            // -------------------------------------------------

            if (host in shorteners) {

                risk += 15

                reasons.add(
                    "Shortened link hides the real address ($host)"
                )
            }

            // -------------------------------------------------
            // SUSPICIOUS TLD
            // -------------------------------------------------

            if (tld in riskyTlds) {

                risk += 15

                reasons.add(
                    "Link uses a high-risk domain ending (.$tld)"
                )
            }

            // -------------------------------------------------
            // RAW IP ADDRESS
            // -------------------------------------------------

            if (
                Regex(
                    """^\d{1,3}(\.\d{1,3}){3}$"""
                ).matches(host)
            ) {

                risk += 25

                reasons.add(
                    "Link goes to a raw IP address"
                )
            }

            // -------------------------------------------------
            // PUNYCODE / LOOK-ALIKE DOMAIN
            // -------------------------------------------------

            if (
                host.startsWith("xn--") ||
                host.contains(".xn--")
            ) {

                risk += 20

                reasons.add(
                    "Link uses look-alike characters"
                )
            }

            // -------------------------------------------------
            // HTTP
            // -------------------------------------------------

            if (
                normalized.startsWith(
                    "http://",
                    ignoreCase = true
                )
            ) {

                risk += 5

                reasons.add(
                    "Link is not encrypted (http)"
                )
            }

            // -------------------------------------------------
            // APK DOWNLOAD
            // -------------------------------------------------

            if (
                normalized.contains(
                    ".apk",
                    ignoreCase = true
                )
            ) {

                risk += 30

                reasons.add(
                    "Link downloads an app file (APK)"
                )
            }

            // -------------------------------------------------
            // MANY HYPHENS
            // -------------------------------------------------

            if (
                host.count { it == '-' } >= 3
            ) {

                risk += 10

                reasons.add(
                    "Link domain has many hyphens"
                )
            }

            // -------------------------------------------------
            // BRAND IMPERSONATION
            // -------------------------------------------------

            brands.forEach { (brand, official) ->

                if (
                    host.contains(brand) &&
                    baseDomain !in official
                ) {

                    risk += 25

                    reasons.add(
                        "Link pretends to be \"$brand\" but is not its official site"
                    )
                }
            }
        }

        return risk to reasons.distinct()
    }

    // =========================================================
    // CATEGORY DETECTION
    // =========================================================

    private fun detectCategory(
        text: String,
        hasRiskyLink: Boolean,
        risk: Int
    ): ScamCategory {

        return when {

            risk < 40 &&
                    !hasRiskyLink -> {

                ScamCategory.NONE
            }

            has(
                text,
                "kyc",
                "account blocked",
                "account suspended",
                "bank account",
                "debit card",
                "net banking"
            ) -> {

                ScamCategory.BANK_KYC
            }

            has(
                text,
                "otp",
                "one time password",
                "verification code",
                "cvv",
                "upi pin",
                "mpin"
            ) -> {

                ScamCategory.OTP_THEFT
            }

            has(
                text,
                "upi",
                "collect request",
                "scan qr",
                "pay now",
                "send money",
                "payment"
            ) -> {

                ScamCategory.UPI_PAYMENT
            }

            has(
                text,
                "lottery",
                "prize",
                "winner",
                "you won",
                "reward",
                "gift voucher"
            ) -> {

                ScamCategory.PRIZE
            }

            has(
                text,
                "courier",
                "parcel",
                "package",
                "delivery",
                "customs"
            ) -> {

                ScamCategory.COURIER
            }

            has(
                text,
                "investment",
                "guaranteed return",
                "double your money",
                "forex",
                "bitcoin",
                "stock tips"
            ) -> {

                ScamCategory.INVESTMENT
            }

            has(
                text,
                "income tax",
                "cyber crime",
                "cybercrime",
                "arrest",
                "warrant",
                "legal notice",
                "police action"
            ) -> {

                ScamCategory.GOVERNMENT
            }

            hasRiskyLink -> {

                ScamCategory.PHISHING_LINK
            }

            else -> {

                ScamCategory.GENERAL
            }
        }
    }

    // =========================================================
    // RECOMMENDED ACTIONS
    // =========================================================

    private fun actionsFor(
        category: ScamCategory,
        level: String
    ): List<String> {

        if (level == "LOW") {

            return listOf(

                "No major scam signs were found.",

                "Still verify unexpected requests with the company directly."
            )
        }

        val actions =
            mutableListOf(

                "Do not click any link or reply to this message.",

                "Block the sender."
            )

        actions.add(

            when (category) {

                ScamCategory.OTP_THEFT ->
                    "Never share an OTP, PIN or CVV. Real banks never ask for them."

                ScamCategory.BANK_KYC ->
                    "Call your bank on the number printed on your card or official app."

                ScamCategory.UPI_PAYMENT ->
                    "Never approve a collect request or enter your UPI PIN to receive money."

                ScamCategory.PRIZE ->
                    "Real prizes never ask you to pay a fee first."

                ScamCategory.COURIER ->
                    "Check the parcel on the courier's official app or website."

                ScamCategory.INVESTMENT ->
                    "No genuine investment guarantees returns. Check SEBI registration."

                ScamCategory.GOVERNMENT ->
                    "Government agencies do not demand money or threaten arrest by message or call."

                ScamCategory.PHISHING_LINK ->
                    "Type the company's website address yourself instead of using the link."

                else ->
                    "Verify the sender through an official channel before acting."
            }
        )

        if (level == "HIGH") {

            actions.add(
                "If you already shared details or paid, call 1930 or report at cybercrime.gov.in immediately."
            )
        }

        return actions
    }

    // =========================================================
    // MAIN ENTRY
    // =========================================================

    suspend fun assess(
        text: String,
        source: InputSource,

        /*
         * LocalThreatIntelligence is now the default provider.
         *
         * This means SMS, screenshot and QR analysis can use
         * threat intelligence without changing every screen.
         */
        intelligence: ThreatIntelligence =
            LocalThreatIntelligence

    ): ScamShieldResult {

        // -----------------------------------------------------
        // 1. EXISTING LOCAL MESSAGE ANALYSIS
        // -----------------------------------------------------

        val local =
            analyzeMessage(text)

        val reasons =
            local.indicators.toMutableList()

        var risk =
            local.riskPercentage

        // -----------------------------------------------------
        // 2. LOCAL URL ANALYSIS
        // -----------------------------------------------------

        val (
            urlRisk,
            urlReasons
        ) =
            analyzeUrls(local.links)

        risk += urlRisk

        reasons.addAll(urlReasons)

        // -----------------------------------------------------
        // 3. QR PAYMENT CHECK
        // -----------------------------------------------------

        if (
            source == InputSource.QR
        ) {

            val lower =
                text.trim().lowercase()

            if (
                lower.startsWith("upi://")
            ) {

                risk += 30

                reasons.add(
                    "QR code starts a UPI payment. Scanning a QR code is never needed to receive money."
                )
            }
        }

        // -----------------------------------------------------
        // 4. THREAT INTELLIGENCE
        // -----------------------------------------------------

        if (
            local.links.isNotEmpty()
        ) {

            val verdict =
                try {

                    intelligence.checkUrls(
                        local.links
                    )

                } catch (_: Exception) {

                    null
                }

            if (
                verdict != null &&
                verdict.malicious
            ) {

                /*
                 * A confirmed threat from the intelligence
                 * provider raises the final score strongly.
                 */
                risk =
                    maxOf(
                        risk,
                        90
                    )

                reasons.add(
                    "Known threat: ${verdict.reason}"
                )
            }
        }

        // -----------------------------------------------------
        // 5. FINAL RISK
        // -----------------------------------------------------

        risk =
            risk.coerceIn(
                0,
                100
            )

        val level =
            when {

                risk >= 70 ->
                    "HIGH"

                risk >= 40 ->
                    "MEDIUM"

                else ->
                    "LOW"
            }

        // -----------------------------------------------------
        // 6. CATEGORY
        // -----------------------------------------------------

        val category =
            detectCategory(
                text = text.lowercase(),

                hasRiskyLink =
                    urlRisk >= 15,

                risk =
                    risk
            )

        // -----------------------------------------------------
        // 7. FINAL RESULT
        // -----------------------------------------------------

        return ScamShieldResult(

            source =
                source,

            riskPercentage =
                risk,

            level =
                level,

            category =
                category,

            reasons =
                reasons.distinct(),

            actions =
                actionsFor(
                    category,
                    level
                ),

            explanation =
                local.explanation,

            links =
                local.links,

            phoneNumbers =
                local.phoneNumbers,

            emails =
                local.emails
        )
    }
}