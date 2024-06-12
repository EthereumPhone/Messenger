package org.ethereumhpone.chat.components


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.attachments.MediaItem
import org.ethereumhpone.domain.model.Attachment

@Composable
fun GallerySheet(
    attachments: List<Attachment>,
    selectedAttachments: Set<Attachment> = emptySet(),
    onItemClicked: (Attachment) -> Unit,
) {

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()) {
        onItemClicked(Attachment.Image(uri = it!!))
    }

    LaunchedEffect(Unit) {
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        // Camera item, folder button and first image
        item(span = { GridItemSpan(3) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CameraPreview(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .height(240.dp)
                        .weight(1f),
                    onPhotoCaptured = { onItemClicked(Attachment.Image(it)) }
                )

                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Button(
                        shape = RoundedCornerShape(25),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Outlined.Folder, "")
                            Text("Folders")
                        }
                    }

                    attachments.firstOrNull()?.let {
                        GallerySheetItem(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .fillMaxWidth(),
                            attachment = it,
                            isSelected = selectedAttachments.contains(it),
                            itemClicked = { onItemClicked(it) }
                        )
                    }
                }
            }
        }

        // rest of media
        attachments.drop(1).forEach { attachment ->
            item {
                GallerySheetItem(
                    modifier = Modifier.clip(RoundedCornerShape(15.dp)),
                    attachment = attachment,
                    isSelected = selectedAttachments.contains(attachment),
                    itemClicked = { onItemClicked(attachment) }
                )
            }
        }
    }
}


@Composable
private fun GallerySheetItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    isSelected: Boolean,
    itemClicked: () -> Unit
) {
    Box(Modifier.clickable { itemClicked() }) {
        MediaItem(
            modifier = modifier.aspectRatio(1f),
            attachment = attachment
        )

        if (isSelected) {
            Box(modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(0.5f))
            )
        }
    }
}

@Composable
@Preview
fun PreviewGallerySheet() {
    GallerySheet(
        listOf(
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image(),
            Attachment.Image()
        ),
        emptySet(),
        {},
    )
}

