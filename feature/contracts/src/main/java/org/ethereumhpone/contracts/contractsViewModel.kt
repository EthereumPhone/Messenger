package org.ethereumhpone.contracts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.ChatUIState
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.domain.mapper.ContactCursor
import org.ethereumhpone.domain.repository.ContactRepository
import org.ethereumhpone.domain.repository.ConversationRepository
import org.ethereumhpone.domain.repository.SyncRepository
import javax.inject.Inject


@HiltViewModel
class ContactViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val contactRepository: ContactRepository,
    private val syncRepository: SyncRepository
//    private val contactCursor: ContactCursor
): ViewModel() {

    val conversationState: StateFlow<ConversationUIState> = conversationRepository.getConversations()
        .map(ConversationUIState::Success)
        .stateIn(
        scope = viewModelScope,
        initialValue = ConversationUIState.Empty,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    //val contacts: Flow<List<Contact>> = contactRepository.getContacts()
    val contacts: Flow<List<Contact>> = contactRepository.getContacts()



    @SuppressLint("Range")
    private fun getPhoneNumber(contentResolver: ContentResolver, contactId: String): String {
        var phoneNumber = ""

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            cursor.close()
        }

        return phoneNumber
    }

    @SuppressLint("Range")
    private fun getPhotoUriForContact(contactId: String,contentResolver: ContentResolver): String? {
        val photoCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.Data.CONTACT_ID + " = ?",
            arrayOf(contactId), null
        )

        photoCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                if (photoUri != null) {
                    return photoUri
                }
            }
        }

        return null
    }

    @SuppressLint("Range")
    fun getData15ForContact(contactId: String,contentResolver: ContentResolver): String? {
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.DATA15)
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA15))
            }
        }
        return null
    }


}

sealed interface ConversationUIState {
    object Loading : ConversationUIState
    object Empty : ConversationUIState
    data class Success(val conversations: List<Conversation>): ConversationUIState
}
