package org.ethereumhpone.data.services

import android.os.RemoteCallbackList
import android.util.Log
import org.ethereumhpone.ipc.IIdentityMessageCallback
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton registry that holds [RemoteCallbackList]s of [IIdentityMessageCallback]
 * per caller key. Uses [RemoteCallbackList] for automatic death-recipient cleanup
 * when a client process dies.
 */
object IdentityCallbackRegistry {

    private const val TAG = "IdentityCallbackReg"

    private val callbacks = ConcurrentHashMap<String, RemoteCallbackList<IIdentityMessageCallback>>()

    fun register(callerKey: String, callback: IIdentityMessageCallback) {
        val list = callbacks.getOrPut(callerKey) { RemoteCallbackList() }
        list.register(callback)
        Log.d(TAG, "Registered callback for $callerKey")
    }

    fun unregister(callerKey: String, callback: IIdentityMessageCallback) {
        callbacks[callerKey]?.unregister(callback)
        Log.d(TAG, "Unregistered callback for $callerKey")
    }

    fun notifyNewMessages(callerKey: String, count: Int) {
        val list = callbacks[callerKey] ?: return
        val n = list.beginBroadcast()
        try {
            for (i in 0 until n) {
                try {
                    list.getBroadcastItem(i).onNewMessages(count)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to notify callback $i for $callerKey", e)
                }
            }
        } finally {
            list.finishBroadcast()
        }
        Log.d(TAG, "Notified $n callback(s) for $callerKey: $count new message(s)")
    }
}
