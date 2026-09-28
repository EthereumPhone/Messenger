package org.ethereumhpone.database.model.relation

import org.ethereumhpone.database.model.MessageEntity
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumphone.model.DeliveryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.xmtp.android.library.libxmtp.DecodedMessage.MessageDeliveryStatus

class CompositeMessageTest {

    private fun message(
        senderInboxId: String = "sender-inbox",
        deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.PUBLISHED,
    ) = MessageEntity(
        id = "msg-1",
        threadId = "thread-1",
        senderInboxId = senderInboxId,
        date = 2_000L,
        dateSent = 1_000L,
        body = "hello",
        replyReference = null,
        deliveryStatus = deliveryStatus,
        isMe = true,
    )

    @Test
    fun `missing recipient falls back to sender inbox id instead of crashing`() {
        val external = CompositeMessage(
            message = message(senderInboxId = "my-inbox"),
            recipient = null,
            reactions = emptyList(),
        ).toExternalMessage()

        assertEquals("my-inbox", external.recipient.id)
        assertEquals("", external.recipient.address)
        assertNull(external.recipient.ens)
        assertNull(external.recipient.contact)
        assertEquals("hello", external.body)
        assertEquals("thread-1", external.threadId)
        assertEquals(1_000L, external.dateSent.toEpochMilliseconds())
        assertEquals(2_000L, external.date.toEpochMilliseconds())
    }

    @Test
    fun `present recipient is mapped`() {
        val external = CompositeMessage(
            message = message(),
            recipient = RecipientWithContact(
                recipientEntity = RecipientEntity(
                    inboxId = "sender-inbox",
                    address = "0xabc",
                    ens = "alice.eth",
                    contactLookupKey = null,
                ),
                contactEntity = null,
            ),
            reactions = emptyList(),
        ).toExternalMessage()

        assertEquals("sender-inbox", external.recipient.id)
        assertEquals("0xabc", external.recipient.address)
        assertEquals("alice.eth", external.recipient.ens)
    }

    @Test
    fun `every stored XMTP delivery status maps to an app delivery status`() {
        // ALL is only a query filter in the XMTP SDK and is never stored on a message.
        MessageDeliveryStatus.entries
            .filter { it != MessageDeliveryStatus.ALL }
            .forEach { status ->
                val external = CompositeMessage(message(deliveryStatus = status), null, emptyList())
                    .toExternalMessage()
                assertEquals(DeliveryStatus.valueOf(status.name), external.deliveryStatus)
            }
    }
}
