package org.ethereumhpone.chat.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.attachments.MediaItem
import org.ethereumhpone.domain.model.Attachment

@Composable
fun GallerySheet(
    attachments: List<Attachment>,
    selectedAttachments: Set<Attachment>,
    onItemClicked: (Attachment) -> Unit,
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        // Camera item
        item {
            CameraPreview(
                onPhotoCaptured = {
                    onItemClicked(Attachment.Image(it))
                }
            )
        }

        // folder button and first media item
        item {
            Column {

            }
        }

        // rest of media
        items(attachments) {

            Box {
                MediaItem(attachment = it, itemClicked = onItemClicked)

                if (selectedAttachments.contains(it)) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(0.5f))
                    )
                }
            }
        }
    }
}
