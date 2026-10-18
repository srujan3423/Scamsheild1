package com.scamshield.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        // Only handle phone-state broadcasts.
        if (
            intent.action !=
            TelephonyManager.ACTION_PHONE_STATE_CHANGED
        ) {
            return
        }

        val state =
            intent.getStringExtra(
                TelephonyManager.EXTRA_STATE
            )

        // We only need to process the call when
        // the phone starts ringing.
        if (
            state !=
            TelephonyManager.EXTRA_STATE_RINGING
        ) {
            return
        }

        val incomingNumber =
            intent.getStringExtra(
                TelephonyManager.EXTRA_INCOMING_NUMBER
            )

        // Some Android versions/carriers may not provide
        // the incoming number.
        if (incomingNumber.isNullOrBlank()) {
            return
        }

        val appContext =
            context.applicationContext

        val storage =
            ScanStorage(appContext)

        // Normalize the number so that different formats
        // are treated as the same caller.
        val normalizedNumber =
            storage.normalizePhoneNumber(
                incomingNumber
            )

        if (normalizedNumber.isBlank()) {
            return
        }

        // Number of previous suspicious calls/messages
        // associated with this caller.
        val suspiciousCount =
            storage.getSuspiciousCallerCount(
                normalizedNumber
            )

        // Check whether this number has been identified
        // as suspicious.
        val isSuspicious =
            storage.isSuspiciousCaller(
                normalizedNumber
            )

        // A caller with previous suspicious activity
        // is considered a known scammer.
        val isKnownScammer =
            suspiciousCount > 0

        // Show ScamShield warning immediately.
        if (isSuspicious || isKnownScammer) {

            CallWarningNotification.show(
                context = appContext,
                phoneNumber = incomingNumber,
                knownScammer = isKnownScammer,
                suspiciousCount = suspiciousCount
            )
        }
    }
}