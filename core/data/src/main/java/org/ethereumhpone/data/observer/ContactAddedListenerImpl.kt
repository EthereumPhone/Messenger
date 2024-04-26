package org.ethereumhpone.data.observer

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

private val contactsURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI


fun ContentResolver.observe(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            trySend(selfChange)
        }
    }
    registerContentObserver(contactsURI, true, observer)
    trySend(false)
    awaitClose {
        unregisterContentObserver(observer)
    }
}
