package org.ethereumhpone.data.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.media.RingtoneManager
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.ethereumhpone.data.R
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.data.receiver.MarkSeenReceiver
import javax.inject.Inject
import kotlin.math.abs

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
    private val messageDao: MessageDao,
): org.ethereumhpone.domain.manager.NotificationManager {

    companion object {
        private const val TAG = "NotificationManager"
        private const val MAX_SUMMARY_LINES = 4
        private const val GROUP_KEY_XMTP = "xmtp_message_group"
        private const val SUMMARY_NOTIFICATION_ID = 100_001
        private const val CHANNEL_PREFIX = "dgen1_messenger"

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
        Log.d(TAG, "XMTP notification update requested for thread $threadId")

        if (!notifications(threadId)) {
            Log.d(TAG, "Notifications disabled for thread $threadId")
            return
        }

        if (!permissionManager.hasNotifications()) {
            Log.d(TAG, "Notification permission missing; skipping update")
            return
        }

        val unreadConversations = conversationRepository.getUnreadConversations().first()
        if (unreadConversations.isEmpty()) {
            Log.d(TAG, "No unread conversations; nothing to notify")
            return
        }

        // Resolve or create the appropriate channel for current user settings
        val channelId = resolveNotificationChannelId()

        // Build/update a notification for each unread conversation
        unreadConversations.forEach { convo ->
            val convoId = convo.id
            val notificationId = getNotificationIdFromThreadId(convoId)
            val pendingIntent = contentPendingIntent(context, convoId)

            val defaultTitle = context.applicationInfo.loadLabel(context.packageManager)?.toString().orEmpty()
            val header = convo.getHeader().ifBlank { defaultTitle }

            // Accumulate unseen messages for the conversation; if none, fall back to the last few messages
            val unseen = messageDao.getUnseenMessagesForThread(convoId)
            val messages = if (unseen.isNotEmpty()) unseen else messageDao.getMessagesForThread(convoId).takeLast(5)

            val bigText = messages.joinToString("\n") { msg ->
                val prefix = if (msg.isMe) context.getString(R.string.notification_you) + ": " else ""
                val body = msg.body
                prefix + body
            }.ifBlank { convo.getSummary() }

            val whenTs = (convo.lastMessage?.dateSent?.toEpochMilliseconds()
                ?: messages.lastOrNull()?.dateSent
                ?: System.currentTimeMillis())

            val markReadAction = buildMarkReadAction(convoId)

            val builder = NotificationCompat.Builder(context, channelId)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setSmallIcon(R.drawable.ic_sms_light)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(whenTs)
                .setContentTitle(header)
                .setContentText(convo.getSummary())
                .setGroup(GROUP_KEY_XMTP)
                .addAction(markReadAction)

            // On pre-O devices, set sound/vibration on the notification itself
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val (soundUri, vibrate) = resolveSoundAndVibration()
                if (soundUri != null) {
                    builder.setSound(soundUri)
                }
                if (vibrate) {
                    builder.setVibrate(VIBRATE_PATTERN)
                } else {
                    builder.setVibrate(longArrayOf(0L))
                }
            }

            pendingIntent?.let { builder.setContentIntent(it) }

            val bigTextStyle = NotificationCompat.BigTextStyle()
                .setBigContentTitle(header)
                .bigText(bigText)
            builder.setStyle(bigTextStyle)

            notificationManager.notify(notificationId, builder.build())
        }

        // Post/update a group summary if more than one unread conversation exists
        if (unreadConversations.size > 1) {
            val summaryTitle = context.getString(R.string.notification_multiple_title, unreadConversations.size)
            val summaryText = context.getString(R.string.notification_multiple_summary)

            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(summaryTitle)
                .setSummaryText(summaryText)

            unreadConversations
                .sortedByDescending { it.lastMessage?.dateSent?.toEpochMilliseconds() ?: Long.MIN_VALUE }
                .take(MAX_SUMMARY_LINES)
                .forEach { conversation ->
                    val header = conversation.getHeader()
                    val summary = conversation.getSummary()
                    val line = when {
                        summary.isBlank() -> header
                        header.isBlank() -> summary
                        else -> "$header: $summary"
                    }
                    inboxStyle.addLine(line)
                }

            val summary = NotificationCompat.Builder(context, channelId)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setSmallIcon(R.drawable.ic_sms_light)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_XMTP)
                .setGroupSummary(true)
                .setStyle(inboxStyle)
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .build()

            notificationManager.notify(SUMMARY_NOTIFICATION_ID, summary)
        }
    }

    // Helper function to generate a consistent notification ID from a threadId string
    private fun getNotificationIdFromThreadId(threadId: String): Int {
        // Use string hashCode which is consistent for the same string
        // Make sure it's positive by taking absolute value
        return abs(threadId.hashCode())
    }
    
    // Helper function to generate a consistent request code from a threadId string
    private fun getRequestCodeFromThreadId(threadId: String): Int {
        // Make it different from the notification ID by adding a constant
        return abs(threadId.hashCode() + 1000)
    }

    override fun notifyFailed(threadId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun createNotificationChannel(threadId: String) {
        // Ensure a channel aligned with current user settings exists
        resolveNotificationChannelId()
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


    // Returns the channelId for current settings, creating it if necessary.
    private suspend fun resolveNotificationChannelId(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return DEFAULT_CHANNEL_ID
        }

        val (soundUri, vibrate) = resolveSoundAndVibration()
        val soundKey = soundUri?.toString()?.hashCode()?.let { kotlin.math.abs(it) }?.toString() ?: "silent"
        val vibKey = if (vibrate) "v" else "nv"
        val channelId = "${CHANNEL_PREFIX}_${soundKey}_$vibKey"

        // Check if channel exists
        val existing = notificationManager.notificationChannels.firstOrNull { it.id == channelId }
        if (existing != null) return channelId

        // Build channel attributes
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = if (soundUri != null) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(
            channelId,
            name,
            importance
        ).apply {
            description = descriptionText
            enableVibration(vibrate)
            if (vibrate) {
                vibrationPattern = VIBRATE_PATTERN
            }
            if (soundUri != null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            } else {
                setSound(null, null)
            }
        }

        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    // Determine sound URI and whether to vibrate based on user preferences
    private suspend fun resolveSoundAndVibration(): Pair<Uri?, Boolean> {
        val prefs = messengerPreferences.prefs.first()
        val tone = prefs.ringTone

        val soundUri: Uri? = when {
            tone.isNullOrBlank() -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> kotlin.runCatching { Uri.parse(tone) }.getOrNull()
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        // Default to enabling vibration with sound; can be split into a dedicated user pref later.
        val vibrate = true
        return Pair(soundUri, vibrate)
    }

    private fun buildMarkReadAction(threadId: String): NotificationCompat.Action {
        val intent = Intent(context, MarkSeenReceiver::class.java).apply {
            putExtra("threadId", threadId)
        }
        val requestCode = getRequestCodeFromThreadId(threadId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_mark_read),
            pendingIntent
        ).build()
    }
}


private fun Context.ensureNotificationChannelExists() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(
        DEFAULT_CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = getString(R.string.notification_channel_description)
    }

    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}


private fun contentPendingIntent(context: Context, threadId: String): PendingIntent? {
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

    // Generate a consistent request code from the threadId
    val requestCode = abs(threadId.hashCode())

    // Create the PendingIntent with the resolved launcher activity
    return PendingIntent.getActivity(
        context,
        requestCode,
        Intent().apply {
            action = Intent.ACTION_VIEW
            setClassName(context.packageName, launcherActivityClassName)
            putExtra("threadId", threadId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
