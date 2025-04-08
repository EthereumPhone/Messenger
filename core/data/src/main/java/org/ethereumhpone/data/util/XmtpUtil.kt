import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmtp.android.library.codecs.Attachment
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

