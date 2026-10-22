package com.wizzwuz.whatsappsim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicInteger

enum class AppType(val idName: String) {
    WHATSAPP("whatsapp"),
    GOOGLE_MESSAGES("google_messages");

    companion object {
        fun fromId(id: String?): AppType {
            return values().firstOrNull { it.idName.equals(id, ignoreCase = true) } ?: WHATSAPP
        }
    }
}

object NotificationHelper {

    private const val CHANNEL_ID_WA = "whatsapp_channel"
    private const val CHANNEL_NAME_WA = "WhatsApp"

    private const val CHANNEL_ID_MESSAGES = "google_messages_channel"
    private const val CHANNEL_NAME_MESSAGES = "Messages"

    private val notificationIdCounter = AtomicInteger(2001)

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // WhatsApp Channel
            val waChannel = NotificationChannel(
                CHANNEL_ID_WA,
                CHANNEL_NAME_WA,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming WhatsApp messages"
                enableLights(true)
                lightColor = Color.parseColor("#25D366")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(waChannel)

            // Google Messages Channel
            val gmChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                CHANNEL_NAME_MESSAGES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming text and SMS messages"
                enableLights(true)
                lightColor = Color.parseColor("#1A73E8")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(gmChannel)
        }
    }

    fun showNotification(context: Context, appType: AppType, sender: String, message: String) {
        createChannels(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action1Intent = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action2Intent = PendingIntent.getActivity(
            context,
            2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId: String
        val smallIcon: Int
        val largeIconRes: Int
        val accentColor: Int
        val subText: String

        when (appType) {
            AppType.WHATSAPP -> {
                channelId = CHANNEL_ID_WA
                smallIcon = R.drawable.ic_whatsapp
                largeIconRes = R.drawable.ic_whatsapp_large
                accentColor = ContextCompat.getColor(context, R.color.wa_green)
                subText = "WhatsApp"
            }
            AppType.GOOGLE_MESSAGES -> {
                channelId = CHANNEL_ID_MESSAGES
                smallIcon = R.drawable.ic_google_messages
                largeIconRes = R.drawable.ic_google_messages_large
                accentColor = ContextCompat.getColor(context, R.color.google_blue)
                subText = "Messages"
            }
        }

        val largeIconBitmap = getBitmapFromVectorDrawable(context, largeIconRes)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(sender)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSubText(subText)
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(contentPendingIntent)
            .addAction(0, context.getString(R.string.action_reply), action1Intent)
            .addAction(0, context.getString(R.string.action_mark_read), action2Intent)

        // For Google Messages with OTP/Code, add Copy Code action if detected
        if (appType == AppType.GOOGLE_MESSAGES && containsVerificationCode(message)) {
            builder.addAction(0, context.getString(R.string.action_copy_code), action2Intent)
        }

        notificationManager.notify(notificationIdCounter.incrementAndGet(), builder.build())
    }

    private fun containsVerificationCode(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("code") || lower.contains("otp") || lower.contains("verification") || lower.contains("login")
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 96,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 96,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
