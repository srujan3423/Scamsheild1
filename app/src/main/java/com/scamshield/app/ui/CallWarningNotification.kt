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


        // --------------------------------------------------------
        // TITLE
        // --------------------------------------------------------

        val title =
            if (knownScammer) {
                "🔴 ScamShield: KNOWN SCAMMER"
            } else {
                "🟠 ScamShield: SUSPICIOUS CALL"
            }


        // --------------------------------------------------------
        // MAIN MESSAGE
        // --------------------------------------------------------

        val message =
            if (knownScammer) {

                "Caller: $phoneNumber • Previous reports: $suspiciousCount"

            } else {

                "Potentially suspicious caller: $phoneNumber"
            }


        // --------------------------------------------------------
        // SAFETY MESSAGE
        // --------------------------------------------------------

        val safetyMessage =
            if (knownScammer) {

                "This number has previously been associated " +
                        "with suspicious activity. Avoid sharing " +
                        "OTP, UPI PIN, passwords or banking details."

            } else {

                "Be careful with this caller. Never share OTPs, " +
                        "UPI PINs, passwords or banking information."
            }


        // --------------------------------------------------------
        // NOTIFICATION
        // --------------------------------------------------------

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
                            "$message\n\n$safetyMessage"
                        )
                )

                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                .setCategory(
                    NotificationCompat.CATEGORY_CALL
                )

                .setAutoCancel(true)

                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )

                .build()


        // --------------------------------------------------------
        // SHOW NOTIFICATION
        // --------------------------------------------------------

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }


    // ============================================================
    // NOTIFICATION CHANNEL
    // ============================================================

    private fun createChannel(
        notificationManager: NotificationManager
    ) {

        val existingChannel =
            notificationManager.getNotificationChannel(
                CHANNEL_ID
            )

        if (existingChannel != null) {
            return
        }


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