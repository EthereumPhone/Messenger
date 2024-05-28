package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.domain.model.Attachment

@Composable
fun AttachmentRow(
    selectedAttachments: List<Attachment>,
    attachmentRemoved: (Attachment) -> Unit,
) {
    LazyRow {
        items(selectedAttachments) { attachment ->
            SelectedAttachment(
                iconClicked = { attachmentRemoved(attachment) }
            ) {
                when(attachment) {
                    is Attachment.Image, is Attachment.Video -> {
                        SmallMediaItem(
                            modifier = Modifier.size(50.dp),
                            attachment = attachment
                        )
                    }
                    is Attachment.Contact -> { SmallContactItem(attachment) }
                }
            }
        }
    }
}

@Composable
fun SmallMediaItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
) {
    when {
        attachment is Attachment.Image -> {
            SubcomposeAsyncImage(
                modifier = modifier,
                model = attachment.getUri(),
                loading = { },
                contentDescription = "",
                contentScale = ContentScale.Crop,
            )
        }

        attachment is Attachment.Video -> {
            Image(
                modifier = modifier,
                painter = rememberAsyncImagePainter(model = attachment.getThumbnail(LocalContext.current)),
                contentDescription = "",
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun SmallContactItem(
    contact: Attachment.Contact
) {
    Row {
        // Icon
        
        Column {
            Text(text = contact.vCard)
            // show contact detail
            Text("")
        }
    }

}

private fun getVCard() {}