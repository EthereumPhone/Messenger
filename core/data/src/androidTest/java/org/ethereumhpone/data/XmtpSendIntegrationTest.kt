package org.ethereumhpone.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xmtp.android.library.Client
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.SendOptions
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.ContentTypeReply
import org.xmtp.android.library.codecs.ContentTypeText
import org.xmtp.android.library.codecs.Reply
import org.xmtp.android.library.codecs.ReplyCodec
import org.xmtp.android.library.messages.PrivateKeyBuilder
import java.io.File
import java.security.SecureRandom
import kotlin.time.Duration.Companion.minutes

/**
 * End-to-end check that the bundled XMTP SDK can register inboxes and send messages on the
 * XMTP network, using the same prepareMessage -> publishMessages flow as MessageRepositoryImpl.
 * Runs against the XMTP dev network with throwaway keys; needs network access on the device.
 */
@RunWith(AndroidJUnit4::class)
class XmtpSendIntegrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dbDir: File

    @Before
    fun setUp() {
        dbDir = File(context.cacheDir, "xmtp-test-${System.nanoTime()}").apply { mkdirs() }
        Client.register(codec = ReplyCodec())
    }

    @After
    fun tearDown() {
        dbDir.deleteRecursively()
    }

    private suspend fun newClient(): Client = Client.create(
        account = PrivateKeyBuilder(),
        options = ClientOptions(
            api = ClientOptions.Api(XMTPEnvironment.DEV, isSecure = true),
            appContext = context,
            dbEncryptionKey = SecureRandom().generateSeed(32),
            dbDirectory = dbDir.absolutePath,
        ),
    )

    @Test
    fun textMessage_isDeliveredToRecipient() = runTest(timeout = 2.minutes) {
        val alice = newClient()
        val bob = newClient()

        val dm = alice.conversations.findOrCreateDmWithIdentity(bob.publicIdentity)
        val messageId = dm.prepareMessage("hello from the upgrade test")
        dm.publishMessages()

        bob.conversations.syncAllConversations()
        val bobDm = bob.conversations.findConversation(dm.id)
        assertNotNull("recipient never saw the conversation", bobDm)
        bobDm!!.sync()

        val received = bobDm.messages().single { it.id == messageId }
        assertEquals("hello from the upgrade test", received.body)
        assertEquals(alice.inboxId, received.senderInboxId)
    }

    @Test
    fun replyMessage_isDeliveredWithReference() = runTest(timeout = 2.minutes) {
        val alice = newClient()
        val bob = newClient()

        val dm = alice.conversations.findOrCreateDmWithIdentity(bob.publicIdentity)
        val originalId = dm.prepareMessage("original")
        val replyId = dm.prepareMessage(
            Reply(reference = originalId, content = "a reply", contentType = ContentTypeText),
            options = SendOptions(contentType = ContentTypeReply),
        )
        dm.publishMessages()

        bob.conversations.syncAllConversations()
        val bobDm = bob.conversations.findConversation(dm.id)!!
        bobDm.sync()

        val received = bobDm.messages().single { it.id == replyId }
        assertEquals(ContentTypeReply, received.encodedContent.type)
        val reply = received.content<Reply>()!!
        assertEquals(originalId, reply.reference)
        assertEquals("a reply", reply.content)
    }
}
