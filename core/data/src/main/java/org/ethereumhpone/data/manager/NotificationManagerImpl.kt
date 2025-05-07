package org.ethereumhpone.data.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import org.ethereumhpone.data.R
import org.ethereumhpone.data.receiver.MarkSeenReceiver
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumphone.model.Conversation
import javax.inject.Inject

private const val TARGET_ACTIVITY_NAME = "org.ethereumhpone.messenger.MainActivity"
private const val DEFAULT_CHANNEL_ID = "dgen1_messenger"
private const val DEEP_LINK_SCHEME_AND_HOST = "https://www.ethereumhpone.messenger.org"
private const val CHAT_PATH = "chat"
private const val NOTIFICATION_REQUEST_CODE = 0


class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val permissionManager: PermissionManager,
    private val conversationRepository: ConversationRepository,
): org.ethereumhpone.domain.manager.NotificationManager {

    companion object {
        const val NOTIFICATIONS_KEY = "notifications"
        const val THREAD_NOTIFICATIONS_KEY = "thread_notifications"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    @SuppressLint("ServiceCast")
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Make sure the default channel has been initialized
        context.ensureNotificationChannelExists()
    }

    override suspend fun update(threadId: String) {

        println("xmtp notification started")

        // check if notifications are disabled
        if(!notifications(threadId)) return

        // check permissions
        if(!permissionManager.hasNotifications()) return


        val unreadConversations = conversationRepository.getUnreadConversations().first()

        if (unreadConversations.isEmpty()) {
            //notificationManager.cancel(threadId.toInt())
            //notificationManager.cancel(threadId.toInt() + 100000)
            //return
        }

        val latestConversation = unreadConversations.maxBy { conversation -> conversation.lastMessage!!.dateSent.toEpochMilliseconds() }

        val header = if (unreadConversations.size > 1) {
            "new messages"
        } else {
            latestConversation.getHeader()
        }

        val subheader = if (unreadConversations.size > 1) {
            "press to view"
        } else {
            latestConversation.getSummary()
        }

        val contentPI = contentPendingIntent(context, threadId.toInt())

        val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
        val seenPI = PendingIntent.getBroadcast(context, threadId.toInt(), seenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        println("xmtp notification building")

        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            //.setColor(colors.theme(lastRecipient).theme)  // Uncomment and adjust if you have theming
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_sms_light)
            .setNumber(unreadConversations.size)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setDeleteIntent(seenPI)
            .setWhen(latestConversation.lastMessage?.dateSent?.toEpochMilliseconds() ?: System.currentTimeMillis())
            .setVibrate(VIBRATE_PATTERN)
            .setContentTitle(header)  // Use recipient's name, fallback to a default string
            .setContentText(subheader)  // Show the message content


        println("xmtp notification sending")
        notificationManager.notify(0, notification.build())

    }

    override fun notifyFailed(threadId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun createNotificationChannel(threadId: String) {
// Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "xmtp messenger"
            val descriptionText = "xmtp messenger notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(DEFAULT_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun buildNotificationChannelId(threadId: String): String {
        return when (threadId) {
            "0" -> DEFAULT_CHANNEL_ID
            else -> "notifications_$threadId"
        }
    }

    override fun getNotificationForBackup(): NotificationCompat.Builder {
        TODO("Not yet implemented")
    }

    private fun getNotificationChannel(threadId: String): NotificationChannel? {
        val channelId = buildNotificationChannelId(threadId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.notificationChannels
                .find { channel -> channel.id == channelId }
        }

        return null
    }

    private fun getChannelIdForNotification(threadId: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getNotificationChannel(threadId)?.id ?: DEFAULT_CHANNEL_ID
        }

        return DEFAULT_CHANNEL_ID
    }

    private suspend fun notifications(threadId: String = "0"): Boolean {
        val threads = messengerPreferences.prefs.first().threadNotificationsId
        val default = threads[NOTIFICATIONS_KEY] ?: true

        return when(threadId) {
            "0" -> default
            else -> threads["$THREAD_NOTIFICATIONS_KEY$threadId"] ?: default
        }
    }
}


private fun Context.ensureNotificationChannelExists() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(
        DEFAULT_CHANNEL_ID,
        "default",
        NotificationManager.IMPORTANCE_DEFAULT,
    )

    // Register the channel with the system
    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}


private fun contentPendingIntent(context: Context, threadId: Int): PendingIntent? {
    // Resolve the main launcher activity
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(context.packageName)
    }
    val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
    val resolveInfo = resolveInfoList.firstOrNull()

    // Ensure we found the launcher activity
    val launcherActivityClassName = resolveInfo?.activityInfo?.name ?: return null

    // Create the PendingIntent with the resolved launcher activity
    return PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        Intent().apply {
            action = Intent.ACTION_VIEW
            setClassName(context.packageName, launcherActivityClassName)
            putExtra("threadId", threadId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
