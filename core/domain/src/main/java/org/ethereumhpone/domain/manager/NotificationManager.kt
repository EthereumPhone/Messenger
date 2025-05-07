package org.ethereumhpone.domain.manager

import androidx.core.app.NotificationCompat

interface NotificationManager {

    suspend fun update(threadId: String)

    fun notifyFailed(threadId: String)

    suspend fun createNotificationChannel(threadId: String = "0")

    fun buildNotificationChannelId(threadId: String): String

    fun getNotificationForBackup(): NotificationCompat.Builder

}