package com.sseotdabwa.buyornot.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.MainActivity
import com.sseotdabwa.buyornot.R
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "FCM"
private const val CHANNEL_ID = "buyornot_default_channel"
private const val CHANNEL_NAME = "살까말까 알림"
private const val DEFAULT_TITLE = "살까말까"
private const val DEFAULT_BODY = "새로운 소식이 도착했어요."

@AndroidEntryPoint
class BuyOrNotMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onNewToken - FCM token: $token")
        }
        // TODO: Send token to server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMessageReceived - from: ${message.from}")
            Log.d(TAG, "onMessageReceived - data payload: ${message.data}")
            Log.d(
                TAG,
                "onMessageReceived - notification: title=${message.notification?.title}, " +
                    "body=${message.notification?.body}",
            )
        }

        val feedId = message.data[FcmKeys.FEED_ID]?.toLongOrNull()
        if (feedId == null) {
            Log.d(TAG, "onMessageReceived - no valid feedId in data, skip notification")
            return
        }

        showFeedNotification(
            feedId = feedId,
            title = message.notification?.title ?: DEFAULT_TITLE,
            body = message.notification?.body ?: DEFAULT_BODY,
        )
    }

    private fun showFeedNotification(
        feedId: Long,
        title: String,
        body: String,
    ) {
        createNotificationChannel()

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(FcmKeys.FEED_ID, feedId.toString())
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                feedId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "showFeedNotification - POST_NOTIFICATIONS not granted, skip posting")
            return
        }

        NotificationManagerCompat.from(this).notify(feedId.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
