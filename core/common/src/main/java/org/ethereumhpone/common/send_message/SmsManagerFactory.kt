package org.ethereumhpone.common.send_message

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.getSystemService

object SmsManagerFactory {

    fun createSmsManager(context: Context, subscriptionId: Int): SmsManager {
        return try {
            if (subscriptionId == -1) {
                context.getSystemService(SmsManager::class.java)
            } else {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            }
        } catch (e: Exception) {
            // Handle exception appropriately
            // For example, log the error and return a default SmsManager instance
            Log.e("SmsManagerFactory", "Error creating SmsManager", e)
            e.printStackTrace()
            context.getSystemService(SmsManager::class.java)
        }
    }
}
