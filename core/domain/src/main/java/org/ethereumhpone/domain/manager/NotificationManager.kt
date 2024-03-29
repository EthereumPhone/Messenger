package org.ethereumhpone.domain.manager

import androidx.core.app.NotificationCompat

interface NotificationManager {

    suspend fun update(threadId: Long)

    fun notifyFailed(threadId: Long)

    suspend fun createNotificationChannel(threadId: Long = 0L)

    fun buildNotificationChannelId(threadId: Long): String

    fun getNotificationForBackup(): NotificationCompat.Builder

}