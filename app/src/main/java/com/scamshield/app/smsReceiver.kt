
package com.scamshield.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScamShieldSMS"
        private const val SAVE_SUSPICIOUS_THRESHOLD = 40
        private const val HIGH_RISK_SOUND_THRESHOLD = 70
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SMS receiver triggered")

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "Ignored action: ${intent.action}")
            return
        }

        val messages =
            Telephony.Sms.Intents.getMessagesFromIntent(intent)

        if (messages.isNullOrEmpty()) {
            Log.d(TAG, "No SMS messages found")
            return
        }

        val sender =
            messages.firstOrNull()?.originatingAddress ?: "Unknown"

        val body = messages.joinToString(separator = "") {
            it.messageBody ?: ""
        }

        if (body.isBlank()) {
            Log.d(TAG, "SMS body is empty")
            return
        }

        Log.d(TAG, "SMS received from: $sender")

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting SMS analysis")

                val result = RiskEngine.assess(
                    text = body,
                    source = InputSource.SMS
                )

                val risk = result.riskPercentage
                Log.d(TAG, "Risk analysis completed: $risk%")

                val storage = ScanStorage(appContext)

                if (risk >= SAVE_SUSPICIOUS_THRESHOLD) {
                    storage.saveScan(
                        message = body,
                        result = result.toScanResult(),
                        sender = sender
                    )
                    Log.d(TAG, "Suspicious SMS saved to history")

                    if (risk >= HIGH_RISK_SOUND_THRESHOLD) {
                        ScamNotifications.showSmsAlert(
                            context = appContext,
                            sender = sender,
                            result = result
                        )
                        Log.d(TAG, "High-risk alert requested")
                    } else {
                        Log.d(
                            TAG,
                            "Saved as suspicious; below sound threshold"
                        )
                    }
                } else {
                    storage.recordSafeScan()
                    Log.d(TAG, "Safe SMS recorded: $risk%")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while processing SMS", e)
            } finally {
                pendingResult.finish()
                Log.d(TAG, "SMS receiver finished")
            }
        }
    }
}
