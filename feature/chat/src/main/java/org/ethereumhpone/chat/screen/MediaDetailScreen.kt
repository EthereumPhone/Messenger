package org.ethereumhpone.chat.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import org.ethereumhpone.chat.util.GalleryMedia
import org.ethereumhpone.chat.util.MediaType

@Composable
fun MediaDetailScreen(
    mediaItem: GalleryMedia,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (mediaItem.type) {
                MediaType.IMAGE -> {
                    Image(
                        painter = rememberAsyncImagePainter(mediaItem.uri),
                        contentDescription = mediaItem.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaType.VIDEO -> {
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(mediaItem.uri))
                            prepare()
                            playWhenReady = true
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            StyledPlayerView(ctx).apply {
                                player = exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        BottomAppBar {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = mediaItem.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}