package org.ethereumhpone.chat.components


import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumhpone.chat.components.attachments.MediaItem
import org.ethereumhpone.domain.model.Attachment
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import kotlin.random.Random

@SuppressLint("SuspiciousIndentation")
@Composable
fun GallerySheet(
    media: List<Uri>,
    attachments: Set<Attachment> = emptySet(),
    onMediaClicked: (Attachment) -> Unit,
) {

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()) {
        it?.let { onMediaClicked(Attachment.Image(uri = it)) }
    }
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
                    onPhotoCaptured = { onMediaClicked(Attachment.Image(uri = it)) }
                )
            }

            item{
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.DARK_GRAY,
                        contentColor=Colors.WHITE,
                    ),
                    shape = RoundedCornerShape(25),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Outlined.Folder, "",tint = Color.White,modifier = Modifier
                            .size(28.dp))
                        Text("Folders",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color =  Colors.WHITE,
                            fontFamily = Fonts.INTER
                        )
                    }
                }
            }

            items(items = media){
                GallerySheetItem(
                    modifier = Modifier.clip(RoundedCornerShape(15.dp)),
                    uri = it,
                    isSelected = attachments.contains(Attachment.Image(uri = it)),
                    itemClicked = { onMediaClicked(Attachment.Image(uri = it)) }
                )
            }
        }
    )

}

@Composable
private fun GallerySheetItem(
    modifier: Modifier = Modifier,
    uri: Uri,
    isSelected: Boolean,
    itemClicked: () -> Unit
) {
    Box(Modifier.clickable { itemClicked() }) {
        MediaItem(
            modifier = modifier.aspectRatio(1f),
            attachment = Attachment.Image(uri)
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
        listOf(Uri.EMPTY, Uri.EMPTY, Uri.EMPTY),
        emptySet(),
        {},
    )
}

