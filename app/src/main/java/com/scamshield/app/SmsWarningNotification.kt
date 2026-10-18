package com.scamshield.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat

object SmsWarningNotification {

    private const val CHANNEL_ID = "scamshield_sms_alerts"

    fun show(
        context: Context,
        sender: String,
        result: ScanResult,
        knownSender: Boolean = false,
        suspiciousCount: Int = 0
    ) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        createChannel(
            context,
            notificationManager
        )

        // Play ScamShield warning sound
        playWarningSound(context)

        val title = when {

            knownSender ->
                "🔴 KNOWN SUSPICIOUS SENDER"

            result.riskPercentage >= 70 ->
                "🔴 ScamShield: DANGEROUS SMS"

            result.riskPercentage >= 40 ->
                "🟠 ScamShield: SUSPICIOUS SMS"

            else ->
                "🟢 ScamShield: SAFE SMS"
        }

        val message =
            if (knownSender) {
                "Sender: $sender • Suspicious messages: $suspiciousCount"
            } else {
                "Sender: $sender • Risk: ${result.riskPercentage}%"
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
                            "$message\n\n${result.explanation}"
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setDefaults(0)
                .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun playWarningSound(
        context: Context
    ) {

        try {

            val player = MediaPlayer()

            val soundUri =
                android.net.Uri.parse(
                    "android.resource://" +
                            context.packageName +
                            "/" +
                            R.raw.scam_alert_high
                )

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ALARM
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )

            player.setDataSource(
                context.applicationContext,
                soundUri
            )

            player.setOnPreparedListener {
                it.start()
            }

            player.setOnCompletionListener {
                it.release()
            }

            player.setOnErrorListener { mp, _, _ ->
                mp.release()
                true
            }

            player.prepareAsync()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun createChannel(
        context: Context,
        notificationManager: NotificationManager
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val audioAttributes =
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_NOTIFICATION
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "ScamShield SMS Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )

            channel.description =
                "Alerts for suspicious SMS messages"

            /*
             * Keep the notification channel silent.
             *
             * The warning sound is played directly
             * by MediaPlayer to avoid duplicate sounds.
             */
            channel.setSound(
                null,
                audioAttributes
            )

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }
}