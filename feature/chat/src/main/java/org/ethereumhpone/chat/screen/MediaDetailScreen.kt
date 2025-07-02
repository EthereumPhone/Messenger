package org.ethereumhpone.chat.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumhpone.chat.components.media.VideoPage
import org.ethereumhpone.chat.R
import org.ethereumhpone.domain.model.Attachment

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaDetailScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    allMedia: List<Attachment>,
    currentIndex: Int
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = currentIndex) { allMedia.size }

    // Sync external navigation with pager
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentIndex) {
            if (pagerState.currentPage > currentIndex) onNext() else onPrevious()
        }
    }
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
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.backicon),
                    contentDescription = "Back",
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
                )
            )
            IconButton(modifier = Modifier.alpha(0f), onClick = { /* More actions */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = Color.Transparent
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(dgenBlack)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (val item = allMedia[page]) {
                    is Attachment.Image -> {
                        Image(
                            painter = rememberAsyncImagePainter(item.getUri()),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is Attachment.Video -> {
                        VideoPage(
                            video = item,
                            onIsPlayingChanged = { playing -> isVideoPlaying = playing },
                            modifier = Modifier
                        )
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

                    is Attachment.Contact -> TODO()
                }
            }
        }
    }
}