package org.ethereumhpone.chat.components.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.util.GalleryMedia

@Composable
fun MediaThumbnail(
    mediaItem: GalleryMedia,
    onClick: () -> Unit,
    isInSelectionMode: Boolean = false,
    isSelected: Boolean = false
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(width = 3.dp, color = dgenTurqoise)
                } else {
                    Modifier
                }
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(mediaItem.uri),
            contentDescription = mediaItem.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (mediaItem.type == MediaType.VIDEO) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
                    .background(
                        color = dgenTurqoise.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = dgenOcean,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        }

        // Selection indicator
        if (isInSelectionMode) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = if (isSelected)
                            dgenTurqoise
                        else
                            dgenOcean.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = dgenOcean,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}