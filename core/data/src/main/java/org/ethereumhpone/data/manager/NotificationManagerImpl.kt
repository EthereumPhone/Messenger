package org.ethereumhpone.data.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.data.R
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumphone.model.Conversation
import org.ethereumphone.model.Message
import javax.inject.Inject
import kotlin.math.abs

private const val DEFAULT_CHANNEL_ID = "xmtp_messages"
private const val NOTIFICATION_GROUP_KEY = "org.ethereumhpone.messenger.MESSAGES"
private const val SUMMARY_NOTIFICATION_ID = 0

class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val permissionManager: PermissionManager,
    private val conversationRepository: ConversationRepository,
    private val messageDao: MessageDao,
): org.ethereumhpone.domain.manager.NotificationManager {

    companion object {
        private const val TAG = "NotificationManager"
        const val NOTIFICATIONS_KEY = "notifications"
        const val THREAD_NOTIFICATIONS_KEY = "thread_notifications"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    @SuppressLint("ServiceCast")
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Main messages channel (high importance for heads-up)
            val messagesChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(messagesChannel)
        }
    }

    /**
     * Updates notifications for a specific thread/conversation.
     * Creates a WhatsApp-style notification with sender name and message preview.
     */
    override suspend fun update(threadId: String) {
        Log.d(TAG, "update() called for threadId: $threadId")

        // Check if notifications are disabled
        if (!notifications(threadId)) {
            Log.d(TAG, "Notifications disabled for thread: $threadId")
            return
        }

        // Check permissions
        if (!permissionManager.hasNotifications()) {
            Log.d(TAG, "No notification permission")
            return
        }

        try {
            // Get the specific conversation for this thread
            val conversation = conversationRepository.getConversation(threadId).first()
            if (conversation == null) {
                Log.w(TAG, "Conversation not found for threadId: $threadId. Falling back to message-based notification.")
                
                // Fallback: build a notification directly from unread messages in the message table
                val unreadEntities = try {
                    messageDao.getUnreadUnseenMessagesForThread(threadId)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to query unread messages for threadId: $threadId", e)
                    emptyList()
                }
                
                if (unreadEntities.isEmpty()) {
                    Log.d(TAG, "No unread messages found for thread (fallback): $threadId")
                    // No-op; nothing to show
                    return
                }
                
                showFallbackNotification(threadId, unreadEntities)
                // Try updating summary as well (may not include this thread if conversation is still absent)
                updateSummaryNotification()
                return
            }

            // Get unread messages for this conversation
            val unreadMessages = getUnreadMessagesForConversation(threadId)
            if (unreadMessages.isEmpty()) {
                Log.d(TAG, "No unread messages for thread: $threadId")
                // Cancel the notification for this thread if there are no unread messages
                cancelNotification(threadId)
                return
            }

            // Build and show the notification
            showConversationNotification(conversation, unreadMessages)

            // Also update the summary notification if there are multiple conversations with unread messages
            updateSummaryNotification()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification for threadId: $threadId", e)
        }
    }

    /**
     * Shows a notification for a specific conversation with its unread messages.
     * Uses MessagingStyle for a WhatsApp-like experience.
     */
    private suspend fun showConversationNotification(conversation: Conversation, unreadMessages: List<Message>) {
        val senderName = conversation.getHeader().ifBlank { 
            // Fallback to address if header is empty
            conversation.recipients.firstOrNull()?.address?.let { shortenAddress(it) } ?: "Unknown"
        }

        // Create the sender Person object
        val senderPerson = Person.Builder()
            .setName(senderName)
            .setIcon(createAvatarIcon(senderName))
            .build()

        // Create MessagingStyle notification
        val messagingStyle = NotificationCompat.MessagingStyle(
            Person.Builder().setName("Me").build()
        ).also { style ->
            // Set conversation title for groups
            if (conversation.isGroup) {
                style.conversationTitle = senderName
                style.isGroupConversation = true
            }

            // Add each unread message
            unreadMessages.forEach { message ->
                val messagePerson = if (message.isMe) {
                    Person.Builder().setName("Me").build()
                } else {
                    // For group chats, show individual sender names
                    val msgSenderName = message.recipient?.let { recipient ->
                        recipient.contact?.name 
                            ?: recipient.ens 
                            ?: shortenAddress(recipient.address)
                    } ?: senderName
                    
                    Person.Builder()
                        .setName(msgSenderName)
                        .setIcon(createAvatarIcon(msgSenderName))
                        .build()
                }

                style.addMessage(
                    message.body.ifBlank { "[Attachment]" },
                    message.dateSent.toEpochMilliseconds(),
                    messagePerson
                )
            }
        }

        val contentPI = createContentPendingIntent(conversation.id)

        val notificationId = getNotificationIdFromThreadId(conversation.id)

        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_light)
            .setStyle(messagingStyle)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setVibrate(VIBRATE_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .setWhen(unreadMessages.lastOrNull()?.dateSent?.toEpochMilliseconds() ?: System.currentTimeMillis())
            .setShowWhen(true)
            // Add quick reply action (optional, can be expanded later)
            .build()

        Log.d(TAG, "Showing notification for $senderName with ${unreadMessages.size} messages")
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Fallback notification when the conversation entity doesn't exist yet.
     * Builds a minimal MessagingStyle notification from MessageEntity rows.
     */
    private fun showFallbackNotification(threadId: String, unreadMessages: List<MessageEntity>) {
        val senderName = "New messages"

        val messagingStyle = NotificationCompat.MessagingStyle(
            Person.Builder().setName("Me").build()
        ).also { style ->
            // Add each unread message
            unreadMessages.forEach { message ->
                val messagePerson = if (message.isMe) {
                    Person.Builder().setName("Me").build()
                } else {
                    val fallbackName = shortenAddress(message.senderInboxId)
                    Person.Builder()
                        .setName(fallbackName)
                        .setIcon(createAvatarIcon(fallbackName))
                        .build()
                }

                style.addMessage(
                    message.body.ifBlank { "[Attachment]" },
                    message.dateSent,
                    messagePerson
                )
            }
        }

        val contentPI = createContentPendingIntent(threadId)
        val notificationId = getNotificationIdFromThreadId(threadId)

        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_light)
            .setStyle(messagingStyle)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setVibrate(VIBRATE_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            .setWhen(unreadMessages.lastOrNull()?.dateSent ?: System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        Log.d(TAG, "Showing fallback notification with ${unreadMessages.size} messages")
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Updates the summary notification that groups all conversation notifications.
     * This appears when there are notifications from multiple conversations.
     * Uses getUnreadConversationsForNotifications() to include UNKNOWN consent state.
     */
    private suspend fun updateSummaryNotification() {
        val unreadConversations = conversationRepository.getUnreadConversationsForNotifications().first()
        
        if (unreadConversations.size <= 1) {
            // No need for summary with only one or zero conversations
            notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
            return
        }

        val totalUnreadCount = unreadConversations.count { it.lastMessage != null }

        val summaryText = "${unreadConversations.size} conversations"
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("$totalUnreadCount new messages")
            .setSummaryText(summaryText)

        // Add a line for each conversation
        unreadConversations.take(5).forEach { conversation ->
            val header = conversation.getHeader().ifBlank { "Unknown" }
            val preview = conversation.lastMessage?.body?.take(50) ?: ""
            inboxStyle.addLine("$header: $preview")
        }

        if (unreadConversations.size > 5) {
            inboxStyle.addLine("+ ${unreadConversations.size - 5} more conversations")
        }

        val summaryNotification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_light)
            .setContentTitle("$totalUnreadCount new messages")
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityPendingIntent())
            .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
    }

    /**
     * Gets unread messages for a specific conversation.
     */
    private suspend fun getUnreadMessagesForConversation(threadId: String): List<Message> {
        return try {
            val conversation = conversationRepository.getConversation(threadId).first()
            if (conversation?.lastMessage != null && conversation.lastMessage?.seen != true && conversation.lastMessage?.isMe != true) {
                // For now, just return the last message if it's unread
                // In a more complete implementation, we'd fetch all unread messages from the DB
                listOf(conversation.lastMessage!!)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread messages for thread: $threadId", e)
            emptyList()
        }
    }

    /**
     * Cancels the notification for a specific thread.
     */
    private fun cancelNotification(threadId: String) {
        val notificationId = getNotificationIdFromThreadId(threadId)
        notificationManager.cancel(notificationId)
    }

    /**
     * Creates a simple avatar icon with the first letter of the name.
     */
    private fun createAvatarIcon(name: String): IconCompat? {
        return try {
            val size = 64
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw circular background
            val paint = Paint().apply {
                isAntiAlias = true
                color = getColorForName(name)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            
            // Draw first letter
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = size * 0.5f
                textAlign = Paint.Align.CENTER
            }
            val letter = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(letter, size / 2f, yPos, textPaint)
            
            IconCompat.createWithBitmap(bitmap)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a consistent color based on the name/address.
     */
    private fun getColorForName(name: String): Int {
        val colors = listOf(
            0xFF6200EE.toInt(), // Purple
            0xFF03DAC5.toInt(), // Teal
            0xFFFF5722.toInt(), // Deep Orange
            0xFF4CAF50.toInt(), // Green
            0xFF2196F3.toInt(), // Blue
            0xFFE91E63.toInt(), // Pink
            0xFF9C27B0.toInt(), // Deep Purple
            0xFF00BCD4.toInt(), // Cyan
        )
        return colors[abs(name.hashCode()) % colors.size]
    }

    /**
     * Shortens an Ethereum address for display.
     */
    private fun shortenAddress(address: String): String {
        return if (address.length > 12) {
            "${address.take(6)}...${address.takeLast(4)}"
        } else {
            address
        }
    }

    /**
     * Creates a PendingIntent to open a specific conversation.
     */
    private fun createContentPendingIntent(threadId: String): PendingIntent? {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val resolveInfo = resolveInfoList.firstOrNull()

        val launcherActivityClassName = resolveInfo?.activityInfo?.name ?: return null

        val requestCode = abs(threadId.hashCode())

        return PendingIntent.getActivity(
            context,
            requestCode,
            Intent().apply {
                action = Intent.ACTION_VIEW
                setClassName(context.packageName, launcherActivityClassName)
                putExtra("threadId", threadId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a PendingIntent to open the main activity (inbox).
     */
    private fun createMainActivityPendingIntent(): PendingIntent? {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val resolveInfo = resolveInfoList.firstOrNull()

        val launcherActivityClassName = resolveInfo?.activityInfo?.name ?: return null

        return PendingIntent.getActivity(
            context,
            0,
            Intent().apply {
                action = Intent.ACTION_MAIN
                setClassName(context.packageName, launcherActivityClassName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNotificationIdFromThreadId(threadId: String): Int {
        // Reserve 0 for summary, start thread IDs from 1
        return abs(threadId.hashCode()) + 1
    }

    override fun notifyFailed(threadId: String) {
        // TODO: Implement failed message notification
    }

    override suspend fun createNotificationChannel(threadId: String) {
        // Already handled in init
    }

    override fun buildNotificationChannelId(threadId: String): String {
        return DEFAULT_CHANNEL_ID
    }

    override fun getNotificationForBackup(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_light)
            .setContentTitle("Backup in progress")
            .setContentText("Please wait...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    private suspend fun notifications(threadId: String = "0"): Boolean {
        val threads = messengerPreferences.prefs.first().threadNotificationsId
        val default = threads[NOTIFICATIONS_KEY] ?: true

        return when (threadId) {
            "0" -> default
            else -> threads["$THREAD_NOTIFICATIONS_KEY$threadId"] ?: default
        }
    }
}
