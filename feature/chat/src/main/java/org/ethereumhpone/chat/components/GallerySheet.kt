package org.ethereumhpone.chat.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.model.Attachments

@Composable
fun GallerySheet(
    attachments: List<Attachment>,
    selectedAttachments: Set<Attachment>,
    onItemClicked: (Attachment) -> Unit,
) {


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        // first camera item
        item {
            CameraPreview(
                onPhotoCaptured = {
                    onItemClicked(Attachment.Image(it))
                }
            )
        }
        // show image & thumbnail
        items(attachments) {
            MediaItem(
                attachment = it,
                isSelected = selectedAttachments.contains(it),
                itemClicked = onItemClicked
            )
        }
    }
}
@Composable
fun MediaItem(
    attachment: Attachment,
    isSelected: Boolean = false,
    itemClicked: (Attachment) -> Unit
) {
    when(attachment) {
        //Video
        is Attachment.Video -> {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(model = attachment.getThumbnail(LocalContext.current)),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clickable {
                        itemClicked(attachment)
                    }
                )
                if (isSelected) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(0.5f)))
                }
            }
        }
        //image
        is Attachment.Image -> {
            Box {
                SubcomposeAsyncImage(
                    model = attachment.getUri(),
                    loading = {

                    },
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clickable {
                        itemClicked(attachment)
                    }
                )

                if (isSelected) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(0.5f)))
                }
            }
        }
        //contact
        is Attachment.Contact -> {

        }
    }
}


@Composable
@Preview
fun MediaItemPreview() {
    MediaItem(
        Attachment.Image()
    ) {}
}