package com.scamshield.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object CallWarningNotification {

    private const val CHANNEL_ID =
        "scamshield_call_alerts"

    fun show(
        context: Context,
        phoneNumber: String,
        knownScammer: Boolean,
        suspiciousCount: Int
    ) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        createChannel(notificationManager)

        val title =
            if (knownScammer) {
                "🔴 KNOWN SCAMMER"
            } else {
                "🟠 SUSPICIOUS CALL"
            }

        val message =
            if (knownScammer) {
                "Number: $phoneNumber • Reports: $suspiciousCount"
            } else {
                "Potentially suspicious caller: $phoneNumber"
            }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "$message\n\n" +
                                    "Do not share OTPs, UPI PINs, " +
                                    "passwords or banking information."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun createChannel(
        notificationManager: NotificationManager
    ) {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "ScamShield Call Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

        channel.description =
            "Warnings for suspicious incoming calls"

        notificationManager.createNotificationChannel(
            channel
        )
    }
}