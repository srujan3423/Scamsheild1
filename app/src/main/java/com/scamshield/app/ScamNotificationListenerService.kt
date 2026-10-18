package com.scamshield.app

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class ScamNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "ScamShieldNotification"

        // Apps that normally contain useful communication/message notifications.
        // We still allow other apps through if their notification contains text.
        private val ignoredPackages = setOf(
            "com.scamshield.app",
            "android",
            "com.android.systemui"
        )

        private val ignoredTitles = setOf(
            "ScamShield"
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.d(
            TAG,
            "ScamShield Notification Listener connected"
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        Log.d(
            TAG,
            "ScamShield Notification Listener disconnected"
        )
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {
        try {
            processNotification(sbn)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error processing notification",
                e
            )
        }
    }

    private fun processNotification(
        sbn: StatusBarNotification
    ) {

        val packageName = sbn.packageName

        // Never analyze ScamShield's own notifications.
        if (packageName in ignoredPackages) {
            return
        }

        val notification = sbn.notification
            ?: return

        val extras = notification.extras
            ?: return

        val title =
            extras.getCharSequence(
                Notification.EXTRA_TITLE
            )?.toString()
                ?.trim()
                .orEmpty()

        val text =
            extras.getCharSequence(
                Notification.EXTRA_TEXT
            )?.toString()
                ?.trim()
                .orEmpty()

        val bigText =
            extras.getCharSequence(
                Notification.EXTRA_BIG_TEXT
            )?.toString()
                ?.trim()
                .orEmpty()

        val subText =
            extras.getCharSequence(
                Notification.EXTRA_SUB_TEXT
            )?.toString()
                ?.trim()
                .orEmpty()

        val lines =
            extras.getCharSequenceArray(
                Notification.EXTRA_TEXT_LINES
            )
                ?.joinToString(" ")
                ?.trim()
                .orEmpty()

        /*
         * Combine the text exposed by the notification.
         *
         * Some apps use EXTRA_TEXT.
         * Some use EXTRA_BIG_TEXT.
         * Some messaging apps expose multiple lines.
         */
        val message = buildString {

            if (title.isNotBlank()) {
                append(title)
            }

            if (text.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append(text)
            }

            if (
                bigText.isNotBlank() &&
                !contains(bigText)
            ) {
                if (isNotEmpty()) append("\n")
                append(bigText)
            }

            if (
                subText.isNotBlank() &&
                !contains(subText)
            ) {
                if (isNotEmpty()) append("\n")
                append(subText)
            }

            if (
                lines.isNotBlank() &&
                !contains(lines)
            ) {
                if (isNotEmpty()) append("\n")
                append(lines)
            }
        }
            .trim()

        // Nothing useful was exposed by this notification.
        if (message.isBlank()) {
            return
        }

        // Ignore known system-style titles.
        if (
            title.isNotBlank() &&
            title in ignoredTitles
        ) {
            return
        }

        /*
         * Avoid processing the same notification repeatedly.
         *
         * A notification can be updated several times by an app.
         */
        val notificationKey =
            buildNotificationKey(sbn)

        if (
            NotificationMemory.wasProcessed(
                this,
                notificationKey
            )
        ) {
            return
        }

        NotificationMemory.markProcessed(
            this,
            notificationKey
        )

        Log.d(
            TAG,
            "Notification received from: $packageName"
        )

        Log.d(
            TAG,
            "Notification text: $message"
        )

        // Analyze using the same ScamShield analyzer
        // already used by SMS/message scanning.
        val result =
            analyzeMessage(message)

        Log.d(
            TAG,
            "Risk: ${result.riskPercentage}% | Level: ${result.level}"
        )

        /*
         * IMPORTANT PRIVACY RULE:
         *
         * Normal notifications are analyzed but are NOT
         * permanently stored.
         *
         * Only suspicious notifications are saved.
         */
        if (result.riskPercentage < 40) {
            return
        }

        val appName =
            getApplicationName(
                packageName
            )

        val sender =
            if (appName.isNotBlank()) {
                appName
            } else {
                packageName
            }

        /*
         * Save suspicious notification in the existing
         * ScamShield scan history.
         */
        val storage =
            ScanStorage(applicationContext)

        storage.saveScan(
            message = message,
            result = result,
            sender = sender
        )

        /*
         * Show ScamShield warning notification.
         *
         * We use a separate notification helper below so
         * this feature doesn't interfere with SMS alerts.
         */
        NotificationThreatAlert.show(
            context = applicationContext,
            appName = sender,
            result = result
        )
    }

    private fun buildNotificationKey(
        sbn: StatusBarNotification
    ): String {

        return buildString {

            append(sbn.packageName)
            append("|")
            append(sbn.id)
            append("|")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(sbn.postTime)
            }
        }
    }

    private fun getApplicationName(
        packageName: String
    ): String {

        return try {

            val packageManager =
                applicationContext.packageManager

            val applicationInfo =
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )

            packageManager
                .getApplicationLabel(
                    applicationInfo
                )
                .toString()

        } catch (_: Exception) {

            packageName
        }
    }
}


/*
 * =========================================================
 * NOTIFICATION MEMORY
 * =========================================================
 *
 * Prevents the same notification from being analyzed
 * repeatedly when an app updates its notification.
 *
 * This is temporary local memory only.
 */
object NotificationMemory {

    private const val PREF_NAME =
        "scamshield_notification_memory"

    private const val KEY_PREFIX =
        "processed_"

    private const val MAX_ITEMS =
        500

    fun wasProcessed(
        context: Context,
        key: String
    ): Boolean {

        val preferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        return preferences.getBoolean(
            KEY_PREFIX + key.hashCode(),
            false
        )
    }

    fun markProcessed(
        context: Context,
        key: String
    ) {

        val preferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val all =
            preferences.all

        if (all.size >= MAX_ITEMS) {

            val editor =
                preferences.edit()

            all.keys
                .take(all.size - MAX_ITEMS + 1)
                .forEach {
                    editor.remove(it)
                }

            editor.apply()
        }

        preferences.edit()
            .putBoolean(
                KEY_PREFIX + key.hashCode(),
                true
            )
            .apply()
    }
}


/*
 * =========================================================
 * THREAT NOTIFICATION
 * =========================================================
 */
object NotificationThreatAlert {

    private const val CHANNEL_ID =
        "scamshield_notification_threats"

    private const val CHANNEL_NAME =
        "Notification Threat Alerts"

    private const val CHANNEL_DESCRIPTION =
        "Alerts for suspicious notifications detected by ScamShield"

    private const val NOTIFICATION_ID =
        9001

    fun show(
        context: Context,
        appName: String,
        result: ScanResult
    ) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        createChannel(
            notificationManager
        )

        val intent =
            context.packageManager
                .getLaunchIntentForPackage(
                    context.packageName
                )

        val pendingIntent =
            intent?.let {

                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )
            }

        val riskEmoji =
            when (result.level) {
                "HIGH" -> "🚨"
                "MEDIUM" -> "⚠️"
                else -> "🛡️"
            }

        val title =
            "$riskEmoji ScamShield Alert"

        val content =
            "$appName • ${result.level} • ${result.riskPercentage}% risk"

        val style =
            androidx.core.app.NotificationCompat
                .BigTextStyle()
                .bigText(
                    buildString {

                        append(
                            "Potential scam detected.\n\n"
                        )

                        if (
                            result.indicators.isNotEmpty()
                        ) {

                            append("Detected:\n")

                            result.indicators
                                .take(5)
                                .forEach { indicator ->

                                    append("• ")
                                    append(indicator)
                                    append("\n")
                                }

                            append("\n")
                        }

                        append(
                            result.explanation
                        )
                    }
                )

        val builder =
            androidx.core.app.NotificationCompat
                .Builder(
                    context,
                    CHANNEL_ID
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )
                .setContentTitle(
                    title
                )
                .setContentText(
                    content
                )
                .setStyle(
                    style
                )
                .setPriority(
                    androidx.core.app.NotificationCompat
                        .PRIORITY_HIGH
                )
                .setCategory(
                    androidx.core.app.NotificationCompat
                        .CATEGORY_ERROR
                )
                .setAutoCancel(
                    true
                )

        if (pendingIntent != null) {
            builder.setContentIntent(
                pendingIntent
            )
        }

        notificationManager.notify(
            NOTIFICATION_ID,
            builder.build()
        )
    }

    private fun createChannel(
        notificationManager:
        android.app.NotificationManager
    ) {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val channel =
            android.app.NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                android.app.NotificationManager
                    .IMPORTANCE_HIGH
            )

        channel.description =
            CHANNEL_DESCRIPTION

        notificationManager.createNotificationChannel(
            channel
        )
    }
}