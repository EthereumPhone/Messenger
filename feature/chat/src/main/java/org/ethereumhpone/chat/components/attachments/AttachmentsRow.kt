package org.ethereumhpone.chat.components.attachments

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.ethereumhpone.chat.R
import org.ethereumhpone.domain.model.Attachment

@Composable
fun AttachmentRow(
    selectedAttachments: List<Attachment>,
    attachmentRemoved: (Attachment) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(selectedAttachments) { attachment ->
            SelectedAttachment(
                iconClicked = { attachmentRemoved(attachment) }
            ) {
                when(attachment) {
                    is Attachment.Image, is Attachment.Video -> {
                        MediaItem(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            attachment = attachment
                        )
                    }
                    is Attachment.Contact -> { ContactItem(contact = attachment) }
                }
            }
        }
    }
}





@Preview
@Composable
fun PreviewAttachmentRow() {
    AttachmentRow(
        listOf(
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image()
            )
    ) {}
}
