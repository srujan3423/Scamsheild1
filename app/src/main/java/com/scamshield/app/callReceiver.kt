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

        val incomingNumber =
            intent.getStringExtra(
                TelephonyManager.EXTRA_INCOMING_NUMBER
            )

        if (
            state == TelephonyManager.EXTRA_STATE_RINGING &&
            !incomingNumber.isNullOrBlank()
        ) {

            val storage =
                ScanStorage(
                    context.applicationContext
                )

            val normalizedNumber =
                storage.normalizePhoneNumber(
                    incomingNumber
                )

            val suspiciousCount =
                storage.getSuspiciousCallerCount(
                    normalizedNumber
                )

            val isKnownScammer =
                suspiciousCount > 0

            val isSuspicious =
                storage.isSuspiciousCaller(
                    normalizedNumber
                )

            if (isSuspicious) {

                CallWarningNotification.show(
                    context = context,
                    phoneNumber = incomingNumber,
                    knownScammer = isKnownScammer,
                    suspiciousCount = suspiciousCount
                )
            }
        }
    }
}