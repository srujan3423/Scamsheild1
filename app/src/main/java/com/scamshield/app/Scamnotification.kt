
package com.scamshield.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object ScamNotifications {

    private const val TAG = "ScamNotifications"
    private const val CHANNEL_ID = "scam_radar_alerts_v2"
    private const val CHANNEL_NAME = "ScamShield High-Risk Alerts"
    private const val HIGH_RISK_THRESHOLD = 70

    fun showSmsAlert(
        context: Context,
        sender: String,
        result: ScamShieldResult
    ) {
        // Only high-risk SMS messages trigger an audible notification.
        if (result.riskPercentage < HIGH_RISK_THRESHOLD) {
            Log.d(
                TAG,
                "Sound skipped: risk=${result.riskPercentage}%"
            )
            return
        }

        showAlert(
            context = context,
            title = "SCAMSHIELD: HIGH-RISK THREAT",
            message = "Suspicious SMS from $sender. " +
                    "Risk: ${result.riskPercentage}%. " +
                    "Do not click links or share OTPs.",
            notificationId = 4100
        )
    }

    fun showAlert(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 4101
    ) {
        val appContext = context.applicationContext

        try {
            createHighRiskChannel(appContext)

            if (
                Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted")
                return
            }

            val notification = NotificationCompat.Builder(
                appContext,
                CHANNEL_ID
            )
                .setSmallIcon(appContext.applicationInfo.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(message)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .build()

            NotificationManagerCompat.from(appContext)
                .notify(notificationId, notification)

            Log.d(TAG, "High-risk notification posted")

        } catch (e: Exception) {
            Log.e(TAG, "Could not post notification", e)
        }
    }

    private fun createHighRiskChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Android remembers channel settings once created.
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }

        val soundUri = Uri.parse(
            "android.resource://${context.packageName}/" +
                    "${R.raw.scam_alert_high}"
        )

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Custom radar sound for high-risk ScamShield alerts"
            setSound(soundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(
                0, 250, 120, 250, 120, 500
            )
        }

        manager.createNotificationChannel(channel)
        Log.d(TAG, "High-risk sound channel created")
    }
}
