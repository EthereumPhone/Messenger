package org.ethereumhpone.chat.components.Message.parts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import org.bouncycastle.math.raw.Mod
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

    var offset by remember { mutableIntStateOf(0) }

    // grouping logic
    ImageGridContainer(mmsMediaItems) { offset = it }

    // image clicked
    if (showExpandedMedia) {
        ExpandedMediaDialog(
            mmsMediaItems,
            videoPlayer,
            offset,
            onPlayVideo = { onPlayVideo(it) },
            onDismissRequest = { showExpandedMedia = false }
        )
    }
}


@Composable
private fun ImageGridContainer(
    media: List<MmsMediaItem>,
    imageClickedIndex: (Int) -> Unit // index
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
                        .clickable { imageClickedIndex(index) }
                ) {
                    AsyncImage(
                        model = if (mediaItem.isVideo) mediaItem.uri.getVideoThumbnail(LocalContext.current) else mediaItem.uri,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.butterfly), // preview only
                        modifier = Modifier.fillMaxSize()
                    )

                    // video indicator
                    if(mediaItem.isVideo) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(10.dp, (-10).dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    }

                    // show more indicator
                    if (index == 3 && media.size > 4) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable { imageClickedIndex(0) },
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpandedMediaDialog(
    media: List<MmsMediaItem>,
    videoPlayer: Player,
    indexOffset: Int = 0,
    onPlayVideo: (Uri) -> Unit,
    onDismissRequest: () -> Unit
) {

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        )
    ) {
        val pagerState = rememberPagerState(
            initialPage = indexOffset,
            pageCount = {media.size}
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
            ) { page ->
                //videoPlayer.stop() // stop reproducing video when swiping
                if (media[page].isVideo) {
                    VideoPlayer(videoPlayer)
                } else {
                    AsyncImage(
                        model = media[page].uri,
                        contentDescription = "",
                        placeholder = painterResource(id = R.drawable.butterfly), // preview only
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun VideoPlayer(
    videoPlayer: Player
) {
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            PlayerView(context).also {
                it.player = videoPlayer
                it.controllerShowTimeoutMs = 1500
            }
        },
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(AspectRatio.RATIO_16_9.toFloat())
        )
    }
}


@Composable
@Preview
fun PreviewImageContainerGrid() {
    // image item
    val placeHolderUri = "android.resource://${LocalContext.current.packageName}/${R.drawable.butterfly}"
    val mmsMediaItem = MmsMediaItem(Uri.parse(placeHolderUri), false)

    // video item
    val videoUri = "android.resource://${LocalContext.current.packageName}/${R.raw.sample_video}"
    val mmsVideoItem = MmsMediaItem(Uri.parse(videoUri), true)


    val media = listOf(mmsVideoItem, mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsMediaItem)
    ImageGridContainer(media) {}
}

@Composable
@Preview
fun PreviewExpandedMediaDialog() {
    val placeHolderUri = "android.resource://${LocalContext.current.packageName}/${R.drawable.butterfly}"
    val mmsMediaItem = MmsMediaItem(Uri.parse(placeHolderUri), false)

    // video item
    val videoUri = "android.resource://${LocalContext.current.packageName}/${R.raw.sample_video}"
    val mmsVideoItem = MmsMediaItem(Uri.parse(videoUri), true)

    // player
    val player = ExoPlayer.Builder(LocalContext.current).build()
    player.addMediaItem(MediaItem.fromUri(videoUri))
    player.prepare()

    val media = listOf(mmsMediaItem, mmsMediaItem, mmsMediaItem, mmsVideoItem)

    var showDialog by remember { mutableStateOf(true) }


    Column {
        if (showDialog) {
            ExpandedMediaDialog(
                media,
                player,
                onPlayVideo = { player.play() },
                onDismissRequest = { showDialog = false }
            )
        }
    }
}

@Preview
@Composable
fun PreviewImageThumbnail() {
    val uri = "android.resource://${LocalContext.current.packageName}/${R.raw.sample_video}"
    val videoUri = Uri.parse(uri)

    val thumbnail = videoUri.let {
        val metadata = MediaMetadataRetriever()
        metadata.setDataSource(LocalContext.current, it)
        metadata.frameAtTime
    }

    AsyncImage(model = thumbnail, contentDescription = "")
}

fun Uri.getVideoThumbnail(context: Context): Bitmap? {
    val metadata = MediaMetadataRetriever()
    metadata.setDataSource(context, this)
    return metadata.frameAtTime
}