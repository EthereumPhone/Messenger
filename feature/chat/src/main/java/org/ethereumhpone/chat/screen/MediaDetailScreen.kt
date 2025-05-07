package org.ethereumhpone.chat.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import org.ethereumhpone.chat.components.media.VideoPage
import org.ethereumhpone.chat.util.GalleryMedia
import org.ethereumhpone.chat.util.MediaType
import org.ethereumhpone.chat.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaDetailScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    allMedia: List<GalleryMedia>,
    currentIndex: Int
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = currentIndex) { allMedia.size }

    // Keep external state updated with the pager
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentIndex) {
            // We need to update the current media based on the pager position
            if (pagerState.currentPage > currentIndex) {
                onNext()
            } else if (pagerState.currentPage < currentIndex) {
                onPrevious()
            }
        }
    }

    // Update pager when external state changes
    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex) {
            pagerState.animateScrollToPage(currentIndex)
        }
    }


    var isVideoPlaying by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .background(dgenBlack)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.backicon),
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }
            Text(
                text = "${currentIndex + 1} / ${allMedia.size}",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
            )
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }
            //Spacer(modifier = Modifier.weight(1f))

            // Additional actions for the media item could go here
            // For example, share, delete, etc.

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .background(dgenBlack)
                    .fillMaxSize()
            ) { page ->
                val pageMediaItem = allMedia[page]

                when (pageMediaItem.type) {
                    MediaType.IMAGE -> {
                        Image(
                            painter = rememberAsyncImagePainter(pageMediaItem.uri),
                            contentDescription = pageMediaItem.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MediaType.VIDEO -> {
                        VideoPage(
                            uri = pageMediaItem.uri,
                            onIsPlayingChanged = { playing ->
                                isVideoPlaying = playing
                            },
                            modifier = Modifier
                        )
                        // now you can react to `playing`, e.g. overlay a play icon
                        if (!isVideoPlaying) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // Navigation controls overlaid on content

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(!isVideoPlaying) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = dgenTurqoise
                        )
                    }
                }
                AnimatedVisibility(!isVideoPlaying) {
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = dgenTurqoise
                        )
                    }
                }


                // Spacer(modifier = Modifier.weight(1f))


            }
        }

    }
}