package com.scamshield.app

import android.net.Uri

/**
 * ScamShield local threat-intelligence layer.
 *
 * Performs offline URL reputation heuristics.
 *
 * This is NOT a global malware database.
 * A cloud threat-intelligence provider can be connected later.
 */

/* =========================================================
   URL VERDICT
   ========================================================= */

data class UrlVerdict(
    val malicious: Boolean,
    val reason: String
)

/* =========================================================
   THREAT INTELLIGENCE INTERFACE
   ========================================================= */

interface ThreatIntelligence {

    suspend fun checkUrls(
        urls: List<String>
    ): UrlVerdict?
}

/* =========================================================
   LOCAL THREAT INTELLIGENCE
   ========================================================= */

object LocalThreatIntelligence : ThreatIntelligence {

    /* ---------------------------------------------------------
       URL SHORTENERS
       --------------------------------------------------------- */

    private val shorteners = setOf(
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

    /* ---------------------------------------------------------
       SUSPICIOUS TLDs
       --------------------------------------------------------- */

    private val riskyTlds = setOf(
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
        "sbs",
        "work",
        "download"
    )

    /* ---------------------------------------------------------
       BRANDS + OFFICIAL DOMAINS
       --------------------------------------------------------- */

    private val brands = mapOf(

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

        "google" to setOf(
            "google.com"
        ),

        "microsoft" to setOf(
            "microsoft.com"
        ),

        "apple" to setOf(
            "apple.com"
        )
    )

    /* ---------------------------------------------------------
       SUSPICIOUS DOMAIN WORDS
       --------------------------------------------------------- */

    private val suspiciousWords = setOf(
        "verify",
        "verification",
        "secure",
        "security",
        "update",
        "account",
        "login",
        "signin",
        "kyc",
        "refund",
        "reward",
        "bonus",
        "cashback",
        "prize",
        "claim",
        "support",
        "wallet",
        "payment",
        "banking",
        "urgent",
        "confirm"
    )

    /* =========================================================
       CHECK MULTIPLE URLs
       ========================================================= */

    override suspend fun checkUrls(
        urls: List<String>
    ): UrlVerdict? {

        for (url in urls) {

            val verdict = checkSingleUrl(url)

            if (verdict != null) {
                return verdict
            }
        }

        return null
    }

    /* =========================================================
       CHECK ONE URL
       ========================================================= */

    private fun checkSingleUrl(
        rawUrl: String
    ): UrlVerdict? {

        val url = rawUrl.trim()

        if (url.isBlank()) {
            return null
        }

        val normalizedUrl =
            if (
                url.startsWith("http://") ||
                url.startsWith("https://")
            ) {
                url
            } else {
                "https://$url"
            }

        val uri =
            try {
                Uri.parse(normalizedUrl)
            } catch (_: Exception) {
                return UrlVerdict(
                    malicious = true,
                    reason = "The URL could not be parsed safely."
                )
            }

        val host =
            uri.host
                ?.lowercase()
                ?.removePrefix("www.")
                ?: return UrlVerdict(
                    malicious = true,
                    reason = "The URL does not contain a valid domain."
                )

        /* -----------------------------------------------------
           1. HTTP
           ----------------------------------------------------- */

        if (
            uri.scheme.equals(
                "http",
                ignoreCase = true
            )
        ) {
            return UrlVerdict(
                malicious = true,
                reason = "The link uses an unsecured HTTP connection."
            )
        }

        /* -----------------------------------------------------
           2. IP ADDRESS
           ----------------------------------------------------- */

        if (isIpAddress(host)) {
            return UrlVerdict(
                malicious = true,
                reason = "The link uses an IP address instead of a normal domain."
            )
        }

        /* -----------------------------------------------------
           3. URL SHORTENER
           ----------------------------------------------------- */

        if (host in shorteners) {
            return UrlVerdict(
                malicious = true,
                reason = "The link uses a URL shortener that hides its final destination."
            )
        }

        /* -----------------------------------------------------
           4. SUSPICIOUS TLD
           ----------------------------------------------------- */

        val tld = host.substringAfterLast(
            ".",
            missingDelimiterValue = ""
        )

        if (tld in riskyTlds) {
            return UrlVerdict(
                malicious = true,
                reason = "The domain uses a suspicious top-level domain."
            )
        }

        /* -----------------------------------------------------
           5. BRAND IMPERSONATION
           ----------------------------------------------------- */

        val brandVerdict =
            checkBrandImpersonation(host)

        if (brandVerdict != null) {
            return brandVerdict
        }

        /* -----------------------------------------------------
           6. SUSPICIOUS DOMAIN WORD
           ----------------------------------------------------- */

        val suspiciousWord =
            suspiciousWords.firstOrNull { word ->
                host.contains(
                    word,
                    ignoreCase = true
                )
            }

        if (suspiciousWord != null) {
            return UrlVerdict(
                malicious = true,
                reason = "The domain contains suspicious wording: $suspiciousWord."
            )
        }

        /* -----------------------------------------------------
           7. TOO MANY SUBDOMAINS
           ----------------------------------------------------- */

        val parts =
            host.split(".")
                .filter { it.isNotBlank() }

        if (parts.size >= 5) {
            return UrlVerdict(
                malicious = true,
                reason = "The link contains an unusually deep subdomain structure."
            )
        }

        /* -----------------------------------------------------
           8. VERY LONG DOMAIN
           ----------------------------------------------------- */

        if (host.length > 60) {
            return UrlVerdict(
                malicious = true,
                reason = "The domain is unusually long and may disguise its destination."
            )
        }

        return null
    }

    /* =========================================================
       BRAND IMPERSONATION
       ========================================================= */

    private fun checkBrandImpersonation(
        host: String
    ): UrlVerdict? {

        for ((brand, officialDomains) in brands) {

            if (!host.contains(brand)) {
                continue
            }

            val isOfficial =
                officialDomains.any { officialDomain ->

                    host == officialDomain ||
                            host.endsWith(
                                ".$officialDomain"
                            )
                }

            if (!isOfficial) {

                return UrlVerdict(
                    malicious = true,
                    reason =
                        "The domain appears to impersonate $brand but is not an official $brand domain."
                )
            }
        }

        return null
    }

    /* =========================================================
       IP ADDRESS CHECK
       ========================================================= */

    private fun isIpAddress(
        host: String
    ): Boolean {

        val parts = host.split(".")

        if (parts.size != 4) {
            return false
        }

        return parts.all { part ->

            val number =
                part.toIntOrNull()

            number != null &&
                    number in 0..255
        }
    }
}