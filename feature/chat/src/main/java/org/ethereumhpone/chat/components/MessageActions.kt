package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Player
import coil.compose.AsyncImage
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.message.parts.VideoPlayer
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.isVideo
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun MessageActionList(
    message: Message,
    focusMode: MutableState<Boolean>,
    onDeleteMessage: () -> Unit = {},
    onDetailMessage: () -> Unit = {},
    isUserMe: Boolean,
) {
    val context = LocalContext.current

    Box(modifier = Modifier
        .graphicsLayer {
            shape = RoundedCornerShape(12.dp)
            clip = true
        }
        .background(Colors.DARK_GRAY)
        .width(200.dp)

    ) {
        Column {
            MessageAction(
                text = "Copy",
                imageVector = Icons.Outlined.ContentCopy,
                onClick = {
                    copyTextToClipboard(context,message.body)
                    focusMode.value = false
                }
            )
            if(isUserMe){
                Divider(color = Colors.GRAY)
                MessageAction(
                    text = "Info",
                    imageVector = Icons.Outlined.Info,
                    onClick = {
                        onDetailMessage()
                    }
                )
            }
            Divider(color= Colors.GRAY)
            MessageAction(
                text = "Delete",
                tint= Colors.ERROR,
                imageVector = Icons.Outlined.Delete,
                onClick = onDeleteMessage
            )
        }
    }
}


@Composable
fun MessageAction(
    text: String,
    tint: Color = Colors.WHITE,
    imageVector: ImageVector,
    onClick: () -> Unit
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Text(text = text, fontSize = 14.sp,fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, color= tint)
        Icon(tint = tint, modifier = Modifier.size(20.dp), imageVector = imageVector, contentDescription = text)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                IconButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint =  Colors.WHITE,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
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


@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}
