package org.ethereumhpone.chat.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ethereumhpone.domain.model.Attachment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheet(
    media: List<Attachment>,
    onDismissRequest: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        // first camera item
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Red)
            )

        }
        // show image & thumbnail
        items(media) {
            MediaItem(it)
        }
    }
}
@Composable
fun MediaItem(
    attachment: Attachment,
    selected: Boolean = false,
    inSelectionMode: Boolean = false
) {
    when(attachment) {
        is Attachment.Video -> {
            Image(
                painter = rememberAsyncImagePainter(model = attachment.getThumbnail(LocalContext.current)),
                contentDescription = ""
            )
        }
        is Attachment.Image -> {
            SubcomposeAsyncImage(
                model = attachment.getUri(),
                loading = {

                },
                contentDescription = "")
        }
        else -> {}
    }
}