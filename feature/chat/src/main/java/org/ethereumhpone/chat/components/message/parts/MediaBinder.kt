package org.ethereumhpone.chat.components.message.parts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import org.ethereumhpone.chat.R
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.isImage
import org.ethereumhpone.database.model.isSmil
import org.ethereumhpone.database.model.isText
import org.ethereumhpone.database.model.isVideo
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.util.Random


@Composable
fun MediaBinder(
    message: Message,
    videoPlayer: Player?,
    onPrepareVideo: (Uri) -> Unit,
    name: String
) {

    val media = remember { message.parts.filter { it.isImage() || it.isVideo() } }
    var showExpandedMedia by remember { mutableStateOf(false) }

    var offset by remember { mutableIntStateOf(0) }

    // grouping logic
    if (media.size <= 3) {
        MediaListContainer(media) {
            offset = it
            showExpandedMedia = true
        }
    } else {
        MediaGridContainer(media) {
            offset = it
            showExpandedMedia = true
        }
    }

    // image clicked
    if (showExpandedMedia) {
        ExpandedMediaDialog(
            media,
            videoPlayer,
            offset,
            onPrepareVideo = { onPrepareVideo(it) },
            onDismissRequest = { showExpandedMedia = false },
            contactname = name
        )
    }
}

@Composable
private fun MediaListContainer(
    media: List<MmsPart>,
    imageClickedIndex: (Int) -> Unit // index
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        media.forEachIndexed { index, item ->
            Box(Modifier.clickable { imageClickedIndex(index) }) {
                AsyncImage(
                    model = if (item.isVideo()) item.getUri().getVideoThumbnail(LocalContext.current) else item.getUri(),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ethos_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp))
                )

                if(item.isVideo()) {
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
            }
        }
    }
}


@Composable
private fun MediaGridContainer(
    media: List<MmsPart>,
    imageClickedIndex: (Int) -> Unit // index
) {
    var index = 0

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(2) {
                    val currentIndex = index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(5))
                            .clickable { imageClickedIndex(currentIndex) }
                    ) {
                        AsyncImage(
                            model = if (media[index].isVideo()) media[index].getUri().getVideoThumbnail(LocalContext.current) else media[index].getUri(),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ethos_placeholder),
                        )

                        // video indicator
                        if(media[index].isVideo()) {
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
                    index++
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpandedMediaDialog(
    media: List<MmsPart>,
    videoPlayer: Player?,
    indexOffset: Int = 0,
    onPrepareVideo: (Uri) -> Unit,
    onDismissRequest: () -> Unit,
    contactname: String
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

        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                IconButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.size(24.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint =  Colors.WHITE,
                        modifier = Modifier.size(24.dp)
                    )
                }
                androidx.compose.material3.Text(
                    textAlign = TextAlign.Center,
                    text = contactname,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.INTER,
                )
            }


            //
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
            ) { page ->
                //videoPlayer.stop() // stop reproducing video when swiping
                if (media[page].isVideo()) {
                    videoPlayer?.let { VideoPlayer(videoPlayer) }
                    onPrepareVideo(media[page].getUri())
                } else {
                    AsyncImage(
                        model = media[page].getUri(),
                        contentDescription = "",
                        placeholder = painterResource(id = R.drawable.ethos_placeholder), // preview only
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (pagerState.pageCount > 1) {
                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        //.align(Alignment.BottomCenter)
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
                it.setShowNextButton(false)
                it.setShowPreviousButton(false)
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
fun PreviewMediaListContainer() {
    val mmsPart = MmsPart()
    val mmsVideoPart = MmsPart(type = "video/")

    val media = listOf(mmsVideoPart, mmsPart, mmsPart, mmsPart, mmsPart)
    MediaListContainer(media) {}
}

@Composable
@Preview
fun PreviewImageContainerGrid() {
    val mmsPart = MmsPart()
    val mmsVideoPart = MmsPart(type = "video/")

    val media = listOf(mmsVideoPart, mmsPart, mmsPart, mmsPart, mmsPart)
    MediaGridContainer(media) {}
}

fun Uri.getVideoThumbnail(context: Context): Bitmap? {
    val metadata = MediaMetadataRetriever()
    try {
        metadata.setDataSource(context, this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return metadata.frameAtTime
}