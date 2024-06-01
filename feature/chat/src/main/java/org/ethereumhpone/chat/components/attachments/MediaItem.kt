package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.R
import org.ethereumhpone.domain.model.Attachment

@Composable
fun MediaItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
) {
    when {
        attachment is Attachment.Image -> {
            AsyncImage(
                modifier = modifier,
                model = attachment.getUri(),
                placeholder =  painterResource(id = R.drawable.ethos),
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