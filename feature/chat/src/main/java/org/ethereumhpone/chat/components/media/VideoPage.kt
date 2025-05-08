package org.ethereumhpone.chat.components.media

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import org.ethereumhpone.domain.model.Attachment

@Composable
fun VideoPage(
    video: Attachment.Video,
    modifier: Modifier = Modifier,
    aspect: Float = 16f/9f,
    onIsPlayingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val uri = video.getUri()

    // build & configure ExoPlayer once per URI
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            if (uri != null){
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true

                // notify parent immediately of the initial state
                onIsPlayingChanged(isPlaying)

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        onIsPlayingChanged(isPlayingNow)
                    }
                })
            }

        }
    }

    DisposableEffect(uri) {
        onDispose {
            onIsPlayingChanged(false)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                StyledPlayerView(ctx).apply {
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.matchParentSize()
        )
    }
}