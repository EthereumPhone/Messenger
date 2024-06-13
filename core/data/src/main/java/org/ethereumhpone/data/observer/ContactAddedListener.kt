package org.ethereumhpone.data.observer

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.callbackFlow

private val contactsURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

fun ContentResolver.observe(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            trySend(selfChange).onFailure {
                Log.e("ERROR", "OBERS")
            }

        }
    }
    registerContentObserver(uri, true, observer)

    awaitClose {
        Log.d("CONTENT OBSERVER", "Unregistering observer for URI: $uri")
        unregisterContentObserver(observer)
    }
}
