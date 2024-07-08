package org.ethereumhpone.chat.components.Message.parts

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import org.ethereumhpone.chat.R
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.isVideo

private data class MmsMediaItem(val uri: Uri, val isVideo: Boolean)

@Composable
fun MediaBinder(
    videoPlayer: Player,
    mmsParts: List<MmsPart>,
    onPlayVideo: (Uri) -> Unit
) {
    val mmsMediaItems = remember { mmsParts.map { MmsMediaItem(it.getUri(), it.isVideo()) } }
    var showExpandedMedia by remember { mutableStateOf(false) }


    when {
        mmsMediaItems.size >= 4 -> ImageGridContainer(mmsMediaItems) {}
        else -> ImageListContainer(mmsMediaItems)
    }

    if (showExpandedMedia) {
        ExpandedMediaDialog(
            mmsMediaItems,
            videoPlayer,
            onPlayVideo = { onPlayVideo(it) },
            onDismissRequest = { showExpandedMedia = false }
        )
    }
}

@Composable
private fun ImageListContainer(
    mmsMediaItems : List<MmsMediaItem>
) {

    LazyColumn {
        mmsMediaItems.forEach { mediaItem ->
            item {
                Box {
                    AsyncImage(
                        model = mediaItem.uri,
                        contentDescription = "",
                        placeholder = painterResource(id = R.drawable.butterfly), // preview only
                    )

                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset((-15).dp, (-15).dp)
                    )
                }
            }
        }
    }
}



@Composable
private fun ImageGridContainer(
    media: List<MmsMediaItem>,
    imageClicked: (Int) -> Unit // index
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        media.take(4).forEachIndexed { index, mediaItem ->
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5))
                        .clickable { imageClicked(index) }
                ) {
                    AsyncImage(
                        model = mediaItem,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.butterfly), // preview only
                        modifier = Modifier.fillMaxSize()
                    )


                    // show more indicator
                    if (index == 3 && media.size > 4) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable { imageClicked(0) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ ${media.size - 4}",
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ExpandedMediaDialog(
    media: List<MmsMediaItem>,
    videoPlayer: Player,
    indexOffset: Int = 0,
    onPlayVideo: (Uri) -> Unit,
    onDismissRequest: () -> Unit
) {

    val context = LocalContext.current

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            media.forEach { mediaItem ->
                item {
                    Box {
                        if (mediaItem.isVideo) {
                            VideoPlayer(videoPlayer)
                        } else {
                            // Image
                            AsyncImage(
                                model = mediaItem.uri,
                                contentDescription = "",
                                placeholder = painterResource(id = R.drawable.butterfly), // preview only
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoPlayer: Player
) {
    Box {
        AndroidView(factory = { context ->
            PlayerView(context).also {
                it.player = videoPlayer
            }
        },
            modifier = Modifier.aspectRatio(AspectRatio.RATIO_16_9.toFloat())
        )
    }
}


@Composable
@Preview
fun PreviewImageListContainer() {
    val palceHolderUri = "android.resource://${LocalContext.current.packageName}/${R.drawable.butterfly}"
    val mmsMediaItem = MmsMediaItem(Uri.parse(palceHolderUri), false)


    val media = listOf(mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsMediaItem)
    ImageListContainer(media)
}


@Composable
@Preview
fun PreviewImageContainerGrid() {
    val palceHolderUri = "android.resource://${LocalContext.current.packageName}/${R.drawable.butterfly}"
    val mmsMediaItem = MmsMediaItem(Uri.parse(palceHolderUri), false)


    val media = listOf(mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsMediaItem)
    ImageGridContainer(media) {}
}

@Composable
@Preview
fun PreviewExpandedMediaDialog() {

    // image item
    val palceHolderUri = "android.resource://${LocalContext.current.packageName}/${R.drawable.butterfly}"
    val mmsMediaItem = MmsMediaItem(Uri.parse(palceHolderUri), false)

    // video item
    val videoUri = "android.resource://${LocalContext.current.packageName}/${R.raw.sample_video}"
    val mmsVideoItem = MmsMediaItem(Uri.parse(videoUri), true)

    // player
    val player = ExoPlayer.Builder(LocalContext.current).build()
    player.addMediaItem(MediaItem.fromUri(videoUri))
    player.prepare()

    val media = listOf(mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsVideoItem)

    ExpandedMediaDialog(
        media,
        player,
        onPlayVideo = { player.play() },
        onDismissRequest = {}
    )
}

@Preview
@Composable
fun PreviewImageThumbnail() {
    val context = LocalContext.current

    val uri = "android.resource://${LocalContext.current.packageName}/${R.raw.sample_video}"
    val videoUri = Uri.parse(uri)

    val thumbnail = videoUri.let {
        val metadata = MediaMetadataRetriever()
        metadata.setDataSource(LocalContext.current, it)
        metadata.frameAtTime
    }

    AsyncImage(model = thumbnail, contentDescription = "")
}



private fun Uri.getVideoThumbnail(context: Context): Bitmap? {
    val metadata = MediaMetadataRetriever()
    metadata.setDataSource(context, this)
    return metadata.frameAtTime
}