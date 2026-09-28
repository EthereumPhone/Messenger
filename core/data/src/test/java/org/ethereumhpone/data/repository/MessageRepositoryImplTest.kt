package org.ethereumhpone.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.MessageDao
import org.ethereumhpone.database.model.MessageEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.xmtp.android.library.Client
import org.xmtp.android.library.Conversation
import org.xmtp.android.library.SendOptions
import org.xmtp.android.library.codecs.ContentTypeReply
import org.xmtp.android.library.codecs.Reply
import org.xmtp.android.library.libxmtp.DecodedMessage.MessageDeliveryStatus

class MessageRepositoryImplTest {

    private val messageDao = mockk<MessageDao>(relaxed = true)
    private val conversation = mockk<Conversation>(relaxed = true)
    private val client = mockk<Client>(relaxed = true)

    private val upsertSlot = slot<List<MessageEntity>>()
    private val upserted = mutableListOf<MessageEntity>()

    private lateinit var repository: MessageRepositoryImpl

    @Before
    fun setUp() {
        mockkObject(XmtpClientManager)
        every { XmtpClientManager.clientState } returns MutableStateFlow(XmtpClientManager.ClientState.Ready)
        every { XmtpClientManager.client } returns client
        every { client.inboxId } returns "my-inbox"

        coEvery { conversation.prepareMessage(any<Any>(), any(), any()) } returns "msg-1"
        coEvery { messageDao.upsertMessages(capture(upsertSlot)) } answers { upserted += upsertSlot.captured }

        repository = MessageRepositoryImpl(
            messageDao = messageDao,
            conversationDao = mockk(relaxed = true),
            reactionDao = mockk(relaxed = true),
            messengerPreferences = mockk(relaxed = true),
            phoneNumberUtils = mockk(relaxed = true),
            syncRepository = mockk(relaxed = true),
            activeConversationManager = mockk(relaxed = true),
            context = mockk(relaxed = true),
            xmtpClientManager = XmtpClientManager,
        )
    }

    @After
    fun tearDown() {
        unmockkObject(XmtpClientManager)
    }

    private suspend fun send(body: String = "hello", replyReference: String? = null) =
        repository.sendMessageWithConversation(
            xmtpConversation = conversation,
            threadId = "thread-1",
            body = body,
            replyReference = replyReference,
            attachments = emptyList(),
            reaction = null,
        )

    @Test
    fun `sending stores the message locally as unpublished and publishes it`() = runTest {
        val id = send()

        assertEquals("msg-1", id)
        val stored = upserted.single()
        assertEquals("msg-1", stored.id)
        assertEquals("thread-1", stored.threadId)
        assertEquals("my-inbox", stored.senderInboxId)
        assertEquals("hello", stored.body)
        assertEquals(MessageDeliveryStatus.UNPUBLISHED, stored.deliveryStatus)
        assertTrue(stored.isMe)
        coVerify(exactly = 1) { conversation.publishMessages() }
    }

    // Regression: replies were prepared without SendOptions, so the SDK tried to encode the
    // Reply with the text codec and threw "Codec type is not registered".
    @Test
    fun `reply is sent with the reply content type and keeps the reference`() = runTest {
        val options = slot<SendOptions>()
        val content = slot<Any>()
        coEvery { conversation.prepareMessage(capture(content), capture(options), any()) } returns "msg-1"

        send(replyReference = "original-msg")

        assertEquals(ContentTypeReply, options.captured.contentType)
        assertEquals("original-msg", (content.captured as Reply).reference)
        assertEquals("original-msg", upserted.single().replyReference)
    }

    // Regression: a failing publish used to propagate out of the send coroutine and crash the app.
    @Test
    fun `failed publish marks the message as failed instead of throwing`() = runTest {
        coEvery { conversation.publishMessages() } throws RuntimeException("network down")

        val id = send()

        assertEquals("msg-1", id)
        assertEquals(
            listOf(MessageDeliveryStatus.UNPUBLISHED, MessageDeliveryStatus.FAILED),
            upserted.map { it.deliveryStatus },
        )
    }

    @Test
    fun `failed prepare returns null and stores nothing`() = runTest {
        coEvery { conversation.prepareMessage(any<Any>(), any(), any()) } throws RuntimeException("boom")

        val id = send()

        assertNull(id)
        assertTrue(upserted.isEmpty())
        coVerify(exactly = 0) { conversation.publishMessages() }
    }

    @Test
    fun `sendMessage returns null when the conversation does not exist`() = runTest {
        coEvery { client.conversations.findConversation("missing") } returns null

        val id = repository.sendMessage(
            threadId = "missing",
            body = "hello",
            replyReference = null,
            attachments = emptyList(),
            reaction = null,
        )

        assertNull(id)
        assertTrue(upserted.isEmpty())
    }

    @Test
    fun `sendMessage publishes through the found conversation`() = runTest {
        coEvery { client.conversations.findConversation("thread-1") } returns conversation

        val id = repository.sendMessage(
            threadId = "thread-1",
            body = "hello",
            replyReference = null,
            attachments = emptyList(),
            reaction = null,
        )

        assertEquals("msg-1", id)
        coVerify(exactly = 1) { conversation.publishMessages() }
    }
}
