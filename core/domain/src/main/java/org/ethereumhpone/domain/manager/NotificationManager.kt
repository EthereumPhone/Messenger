package org.ethereumhpone.domain.manager

import androidx.core.app.NotificationCompat

interface NotificationManager {

    fun update(threadId: Long)

    fun notifyFailed(threadId: Long)

    fun createNotificationChannel(threadId: Long = 0L)

    fun buildNotificationChannelId(threadId: Long): String

    fun getNotificationForBackup(): NotificationCompat.Builder

}