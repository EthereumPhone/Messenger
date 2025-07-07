package org.ethereumhpone.chat.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumhpone.chat.components.media.MediaThumbnail
import org.ethereumphone.dgenlibrary.components.dgenButton
import org.ethereumphone.dgenlibrary.components.dgenTextButton
import org.ethereumhpone.chat.R
import org.ethereumhpone.domain.model.Attachment

@Composable
fun MediaGridScreen(
    mediaItems: List<Attachment>,
    onMediaClick: (Attachment) -> Unit,
    isInSelectionMode: Boolean = false,
    enableSelection: Boolean = true,
    selectedItems: Set<Attachment> = emptySet(),
    toggleSelectionMode: () -> Unit = {},
    selectAllMedia: () -> Unit = {},
    clearSelections: () -> Unit = {},
    onSelectionDone: () -> Unit = {},
    onBack: () -> Unit = {},
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    var selectedAll by remember { mutableStateOf(false) }
    val selectAllColor by animateColorAsState(
        if (selectedAll) primaryColor else primaryColor.copy(alpha = 0.5f),
        tween(300)
    )
    var selectedMediaForPreview by remember { mutableStateOf<Attachment?>(null) }

    selectedMediaForPreview?.let { initialMedia ->
        MediaPreview(
            mediaItems = mediaItems,
            initialItem = initialMedia,
            onBack = { selectedMediaForPreview = null },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onDelete = { attachment ->
                val uriString = when (attachment) {
                    is Attachment.Image -> attachment.getUri()
                    is Attachment.Video -> attachment.getUri()
                    is Attachment.Contact -> null
                }
                Toast.makeText(context, "Delete $uriString", Toast.LENGTH_SHORT).show()
                selectedMediaForPreview = null
            },
            onShare = { attachment ->
                val shareUriString = when (attachment) {
                    is Attachment.Image -> attachment.getUri()
                    is Attachment.Video -> attachment.getUri()
                    is Attachment.Contact -> null
                }
                Toast.makeText(context, "Share $shareUriString", Toast.LENGTH_SHORT).show()
            }
        )
    } ?: Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .background(dgenBlack)
                .fillMaxWidth()
        ) {
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
                        tint = primaryColor
                    )
                }

                if(enableSelection){
                    Crossfade(isInSelectionMode, modifier = Modifier.width(225.dp).animateContentSize()) { selectionOn ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectionOn) {
                                dgenTextButton(
                                    onClick = {
                                        selectedAll = true
                                        selectAllMedia()
                                    },
                                    text = "Select All",
                                    fontColor = selectAllColor,
                                    primaryColor = primaryColor
                                )
                                dgenButton(
                                    onClick = toggleSelectionMode,
                                    text = "Close",
                                    backgroundColor = primaryColor,
                                    fontColor = secondaryColor
                                )
                            } else {
                                dgenButton(
                                    onClick = toggleSelectionMode,
                                    text = "Select",
                                    backgroundColor = primaryColor,
                                    fontColor = secondaryColor
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dgenBlack)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mediaItems) { item ->
                        MediaThumbnail(
                            attachment = item,
                            onClick = {
                                if (isInSelectionMode) {
                                    onMediaClick(item)
                                } else {
                                    selectedMediaForPreview = item
                                }
                            },
                            isInSelectionMode = isInSelectionMode,
                            isSelected = selectedItems.contains(item),
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                }

                // top fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(dgenBlack, Color.Transparent)
                            )
                        )
                        .align(Alignment.TopCenter)
                )
                // bottom fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, dgenBlack)
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }

        AnimatedVisibility(
            visible = isInSelectionMode && selectedItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dgenButton(
                    onClick = {
                        Toast.makeText(context, "onSelectionDone clicked", Toast.LENGTH_SHORT).show() // Select ${selectedItems.size} items
                        onSelectionDone()
                    },
                    text = "Select ${selectedItems.size} items",
                    backgroundColor = primaryColor,
                    fontColor = secondaryColor
                )
                dgenButton(
                    onClick = {
                        selectedAll = false
                        clearSelections()
                    },
                    text = "Clear",
                    backgroundColor = primaryColor,
                    fontColor = secondaryColor
                )
            }
        }
    }
}