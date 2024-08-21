import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumhpone.database.model.MessageReaction
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.Attachment
import org.xmtp.android.library.codecs.Reaction
import org.xmtp.android.library.codecs.ReactionAction
import org.xmtp.android.library.codecs.RemoteAttachment

object XmtpUtil {
    suspend fun saveAttachmentType(context: Context, attachment: Attachment) = withContext(Dispatchers.IO) {
        val name = attachment.filename

        context.openFileOutput(name, Context.MODE_PRIVATE).use {
            it.write(attachment.data.toByteArray())
        }

    }

    suspend fun saveRemoteAttachment(context: Context, remoteAttachment: RemoteAttachment) =
        saveAttachmentType(context, remoteAttachment.load<Attachment>()!!)
}

