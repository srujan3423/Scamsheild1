package com.scamshield.app

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService

class ScamCallScreeningService : CallScreeningService() {

    override fun onScreenCall(
        callDetails: Call.Details

    ) {
        android.util.Log.d(
            "ScamShieldCall",
            "CALL SCREENING SERVICE TRIGGERED"
        )
        try {

            screenCall(callDetails)

        } catch (_: Exception) {

            // Never block or hang a call because of an error here.
            allowCall(callDetails)
        }
    }


    private fun screenCall(
        callDetails: Call.Details
    ) {

        // Every call must get a response, including outgoing ones.
        // Returning without responding can leave the call waiting.
        // callDirection needs Android 10 (API 29). Before that,
        // Android only sent incoming calls to this service.
        val isIncoming =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                callDetails.callDirection ==
                        Call.Details.DIRECTION_INCOMING
            } else {
                true
            }

        if (!isIncoming) {
            allowCall(callDetails)
            return
        }

        val handle = callDetails.handle

        if (
            handle == null ||
            handle.scheme != "tel"
        ) {
            allowCall(callDetails)
            return
        }

        val incomingNumber = handle.schemeSpecificPart

        if (incomingNumber.isNullOrBlank()) {
            allowCall(callDetails)
            return
        }

        val storage = ScanStorage(applicationContext)

        val normalizedNumber =
            storage.normalizePhoneNumber(incomingNumber)

        if (normalizedNumber.isBlank()) {
            allowCall(callDetails)
            return
        }

        val suspiciousCount =
            storage.getSuspiciousCallerCount(normalizedNumber)

        if (suspiciousCount > 0) {

            CallWarningNotification.show(
                context = applicationContext,
                phoneNumber = incomingNumber,
                knownScammer = true,
                suspiciousCount = suspiciousCount
            )
        }

        // For now, always allow the call. The user sees the warning
        // notification and decides whether to answer.
        allowCall(callDetails)
    }


    private fun allowCall(
        callDetails: Call.Details
    ) {

        val builder =
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)

        // setSilenceCall needs Android 10 (API 29).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setSilenceCall(false)
        }

        val response = builder.build()

        respondToCall(
            callDetails,
            response
        )
    }
}