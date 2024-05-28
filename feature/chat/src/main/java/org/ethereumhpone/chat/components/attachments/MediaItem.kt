package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.domain.model.Attachment

@Composable
fun MediaItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    itemClicked: (Attachment) -> Unit
) {
    when {
        attachment is Attachment.Image -> {
            SubcomposeAsyncImage(
                modifier = modifier.clickable { itemClicked(attachment) },
                model = attachment.getUri(),
                loading = { },
                contentDescription = "",
                contentScale = ContentScale.Crop,
            )
        }

        attachment is Attachment.Video -> {
            Image(
                modifier = modifier.clickable { itemClicked(attachment) },
                painter = rememberAsyncImagePainter(model = attachment.getThumbnail(LocalContext.current)),
                contentDescription = "",
                contentScale = ContentScale.Crop,
            )
        }
    }
}