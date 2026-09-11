package com.scamshield.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action !=
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {
            return
        }

        val messages =
            Telephony.Sms.Intents
                .getMessagesFromIntent(intent)

        if (messages.isEmpty()) {
            return
        }

        val messageBody =
            buildString {
                messages.forEach { sms ->
                    append(
                        sms.messageBody ?: ""
                    )
                }
            }

        val sender =
            messages
                .firstOrNull()
                ?.originatingAddress
                ?: "Unknown sender"

        if (messageBody.isBlank()) {
            return
        }

        // Analyze SMS
        val result =
            analyzeMessage(messageBody)

        val storage =
            ScanStorage(
                context.applicationContext
            )

        // IMPORTANT:
        // Check BEFORE saving this current message.
        val wasAlreadySuspicious =
            storage.isKnownSuspiciousSender(sender)

        // Save current scan + sender history
        storage.saveScan(
            message = messageBody,
            result = result,
            sender = sender
        )

        // Show alert only for suspicious/dangerous SMS
        if (result.riskPercentage >= 40) {

            val suspiciousCount =
                storage.getSuspiciousSenderCount(
                    sender
                )

            SmsWarningNotification.show(
                context = context,
                sender = sender,
                result = result,
                knownSender = wasAlreadySuspicious,
                suspiciousCount = suspiciousCount
            )
        }
    }
}