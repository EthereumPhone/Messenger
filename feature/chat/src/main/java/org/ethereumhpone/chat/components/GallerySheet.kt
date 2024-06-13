package org.ethereumhpone.chat.components


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.attachments.MediaItem
import org.ethereumhpone.domain.model.Attachment
import kotlin.random.Random

@SuppressLint("SuspiciousIndentation")
@Composable
fun GallerySheet(
    attachments: List<Attachment>,
    selectedAttachments: Set<Attachment> = emptySet(),
    onItemClicked: (Attachment) -> Unit,
) {

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            item{
                CameraPreview(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .height(240.dp)
                  ,
                    onPhotoCaptured = { onItemClicked(Attachment.Image(it)) }
                )
            }

            items(items = attachments){
                GallerySheetItem(
                    modifier = Modifier.clip(RoundedCornerShape(15.dp)),
                    attachment = it,
                    isSelected = selectedAttachments.contains(it),
                    itemClicked = { onItemClicked(it) }
                )
            }
        }
    )

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
                .clip(RoundedCornerShape(15.dp))
                .background(Color.Black.copy(0.5f))
                .border(
                    2.dp, Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8C7DF7),
                            Color(0xFF6555D8)
                        )
                    ), RoundedCornerShape(15.dp)
                )
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
        emptySet()
    ) {
    }
}

