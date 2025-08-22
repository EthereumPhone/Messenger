package org.ethereumhpone.chat.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.media.VideoPage
import org.ethereumhpone.domain.model.Attachment
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaPreview(
    mediaItems: List<Attachment>,
    initialItem: Attachment,
    onBack: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onDelete: (Attachment) -> Unit,
    onShare: (Attachment) -> Unit
) {
    val initialPage = mediaItems.indexOf(initialItem).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { mediaItems.size }
    // val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }


    Box(modifier = Modifier.fillMaxSize()) {
        if (mediaItems.isNotEmpty()) {
            // Blurry background
            AsyncImage(
                model = (mediaItems[pagerState.currentPage] as? Attachment.Image)?.getUri()
                    ?: (mediaItems[pagerState.currentPage] as? Attachment.Video)?.getUri(),
                contentDescription = "background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(32.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }


        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val media = mediaItems[page]
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when(media) {
                    is Attachment.Image -> {
                        AsyncImage(
                            model = media.getUri(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is Attachment.Video -> {
                        VideoPage(
                            video = media,
                            onIsPlayingChanged = { /* Unused */ }
                        )
                    }
                    is Attachment.Contact -> {
                        // Handle contact
                    }
                }
            }
        }

        if (mediaItems.isNotEmpty()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.backicon),
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }

                // Placeholder for metadata display if needed
            }
        }
    }
} 