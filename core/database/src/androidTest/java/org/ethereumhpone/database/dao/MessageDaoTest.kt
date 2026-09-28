package org.ethereumhpone.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.ethereumhpone.database.MessengerDatabase
import org.ethereumhpone.database.model.ConversationEntity
import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumhpone.database.model.relation.toExternalMessage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xmtp.android.library.libxmtp.DecodedMessage.MessageDeliveryStatus

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

    private lateinit var db: MessengerDatabase

    private val threadId = "thread-1"

    private fun message(senderInboxId: String) = MessageEntity(
        id = "msg-$senderInboxId",
        threadId = threadId,
        senderInboxId = senderInboxId,
        date = 1_000L,
        dateSent = 1_000L,
        body = "hello",
        replyReference = null,
        deliveryStatus = MessageDeliveryStatus.UNPUBLISHED,
        isMe = true,
    )

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MessengerDatabase::class.java,
        ).allowMainThreadQueries().build()

        db.conversationDao.insertConversation(
            ConversationEntity(id = threadId, title = null, createdAt = 0L, clientInbox = "my-inbox")
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    // Regression: this used to throw "Parameter specified as non-null is null:
    // CompositeMessage.<init>, parameter recipient" when sending into a new conversation.
    @Test
    fun getMessages_whenSenderHasNoRecipientRow_doesNotCrash() = runTest {
        db.messageDao.upsertMessages(listOf(message(senderInboxId = "my-inbox")))

        val messages = db.messageDao.getMessages(threadId).first()

        assertEquals(1, messages.size)
        assertNull(messages.single().recipient)
        assertEquals("my-inbox", messages.single().toExternalMessage().recipient.id)
    }

    @Test
    fun getCompositeMessage_whenSenderHasNoRecipientRow_doesNotCrash() = runTest {
        db.messageDao.upsertMessages(listOf(message(senderInboxId = "my-inbox")))

        val message = db.messageDao.getCompositeMessage("msg-my-inbox").first()

        assertNotNull(message)
        assertNull(message!!.recipient)
    }

    @Test
    fun getMessages_picksUpRecipientOnceItIsInserted() = runTest {
        db.messageDao.upsertMessages(listOf(message(senderInboxId = "sender-inbox")))
        db.recipientDao.insertRecipients(
            listOf(RecipientEntity(inboxId = "sender-inbox", address = "0xabc", ens = "alice.eth"))
        )

        val external = db.messageDao.getMessages(threadId).first().single().toExternalMessage()

        assertEquals("0xabc", external.recipient.address)
        assertEquals("alice.eth", external.recipient.ens)
    }
}
