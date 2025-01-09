package org.ethereumhpone.chat.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import org.ethereumhpone.chat.components.attachments.AttachmentRow
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.model.Attachments
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun ChatBottomAppBar(
    attachments: Set<Attachment>,
    onToggleAttachment: (Attachment) -> Unit,
    onSendClick: (String) -> Unit
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusManager = LocalFocusManager.current

    BottomAppBar(
        containerColor = Color.Black
    ) {
        Column {
            /*
AttachmentRow(
                selectedAttachments = attachments.toList(),
                onToggleAttachment = { onToggleAttachment(it) }
            )
             */


            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {

                TextField(
                    shape = RoundedCornerShape(35.dp),
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(35.dp))
                        .animateContentSize(spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ))
                        .border(
                            2.dp,
                            Colors.DARK_GRAY,
                            RoundedCornerShape(35.dp)
                        )
                    ,
                    placeholder = { Text("Type a message") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Colors.WHITE,
                        unfocusedTextColor = Colors.WHITE,
                        focusedContainerColor = Colors.TRANSPARENT,
                        unfocusedContainerColor = Colors.TRANSPARENT,
                        disabledContainerColor = Colors.TRANSPARENT,
                        cursorColor = Colors.WHITE,
                        errorCursorColor = Colors.WHITE,
                        focusedBorderColor = Colors.TRANSPARENT,
                        unfocusedBorderColor = Colors.TRANSPARENT,
                        focusedPlaceholderColor = Colors.GRAY,
                        unfocusedPlaceholderColor = Colors.GRAY,
                    ),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontFamily = Fonts.INTER,
                        fontSize = 18.sp,
                        color = Colors.WHITE,
                    ),
                )


                AnimatedVisibility(
                    textState.text.isNotBlank() || attachments.isNotEmpty(),
                    enter = expandHorizontally(expandFrom = Alignment.Start, clip = false),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start, clip = true)
                ) {
                    IconButton(
                        onClick = { onSendClick(textState.text) },
                        colors = IconButtonDefaults.iconButtonColors(
                            Color(0xFF8C7DF7),
                            Color.White
                        ),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun previewChatBottomAppBar() {
    val set1 = setOf(Attachment.Image("uri".toUri()))
    val set2 = emptySet<Attachment>()

    var selected by remember {  mutableStateOf(true)  }

    Column {
        Button(onClick = {selected = !selected}) { Text("Switch") }
        ChatBottomAppBar(if (selected) set1 else set2, {}, {})
    }
}