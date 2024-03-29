package org.ethereumhpone.data.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.data.util.PhoneNumberUtils
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.MessageRepository
import javax.inject.Inject

class NotificationManager @Inject constructor(
    private val context: Context,
    private val messengerPreferences: MessengerPreferences,
    private val permissionManager: PermissionManager,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val phoneNumberUtils: PhoneNumberUtils
): org.ethereumhpone.domain.manager.NotificationManager {

    companion object {
        const val DEFAULT_CHANNEL_ID = "notifications_default"
        const val BACKUP_RESTORE_CHANNEL_ID = "notifications_backup_restore"

        const val NOTIFICATIONS_KEY = "notifications"
        const val THREAD_NOTIFICATIONS_KEY = "thread_notifications"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    @SuppressLint("ServiceCast")
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    override suspend fun update(threadId: Long) {

        // if check if notifications are disabled
        if(!notifications(threadId)) {
            return
        }

        if(!permissionManager.hasNotifications()) {
            return
        }

    }

    override fun notifyFailed(threadId: Long) {
        TODO("Not yet implemented")
    }

    override fun createNotificationChannel(threadId: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getNotificationChannel(threadId) != null) return

        val channel = when(threadId) {
            0L -> NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "default",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
            }
            else -> {

                val conversation = conversationRepository.getConversation(threadId) ?: return
                val channelId = buildNotificationChannelId(threadId)
                conversation.map {
                    val title = it?.title
                    NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH).apply {
                        enableLights(true)
                        lightColor = Color.WHITE
                        enableVibration(true)
                        vibrationPattern = VIBRATE_PATTERN
                        lockscreenVisibility = 1 //TODO: CHANGE
                    }
                }



            }
        }

        //notificationManager.createNotificationChannel(channel)
    }

    override fun buildNotificationChannelId(threadId: Long): String {
        TODO("Not yet implemented")
    }

    override fun getNotificationForBackup(): NotificationCompat.Builder {
        TODO("Not yet implemented")
    }

    private fun getNotificationChannel(threadId: Long): NotificationChannel? {
        val channelId = buildNotificationChannelId(threadId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.notificationChannels
                .find { channel -> channel.id == channelId }
        }

        return null
    }

    private suspend fun notifications(threadId: Long = 0): Boolean {
        val threads = messengerPreferences.prefs.first().threadNotificationsId
        val default = threads[NOTIFICATIONS_KEY] ?: true

        return when(threadId) {
            0L -> default
            else -> threads["$THREAD_NOTIFICATIONS_KEY$threadId"] ?: default
        }
    }
}





