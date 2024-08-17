import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmtp.android.library.ClientOptions
import org.xmtp.android.library.XMTPEnvironment
import org.xmtp.android.library.codecs.Attachment
import org.xmtp.android.library.codecs.RemoteAttachment


suspend fun saveAttachmentType(context: Context, attachment: Attachment): Uri? = withContext(Dispatchers.IO) {
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(attachment.mimeType)
        ?: return@withContext null

    val name = attachment.filename.takeIf { name -> name.endsWith(extension) }
        ?:"${System.currentTimeMillis()}.$extension"

    context.openFileOutput(name, Context.MODE_PRIVATE).use {
        it.write(attachment.data.toByteArray())
    }

    Uri.fromFile(context.getFileStreamPath(name))
}

suspend fun saveRemoteAttachment(context: Context, remoteAttachment: RemoteAttachment): Uri? =
    saveAttachmentType(context, remoteAttachment.load<Attachment>()!!)
