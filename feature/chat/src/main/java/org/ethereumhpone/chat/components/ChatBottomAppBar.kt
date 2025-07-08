package org.ethereumhpone.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.attachments.AttachmentRow
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.components.DgenCursorTextfield
import com.example.dgenlibrary.ui.theme.pulseOpacity
import android.util.Log

@Composable
fun ChatBottomAppBar(
    attachments: List<Attachment>,
    onToggleAttachment: (Attachment) -> Unit,
    onSendClick: (String) -> Unit,
    hasMultipleLines: MutableState<Boolean> = mutableStateOf(false),
    expand: MutableState<Boolean> = mutableStateOf(false),
    openAction: () -> Unit,
    primaryColor: Color
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current



    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,     // <- Hides the handle
        backgroundColor = Color.Transparent  // <- Optional: also hides selection highlight
    )

    val cursorAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )



    Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize().background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent), startY = 90.0f,
                    endY = 100.0f,))
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .then(if (expand.value) Modifier.fillMaxHeight() else Modifier.heightIn(max= 250.dp)),
            verticalArrangement = Arrangement.Center
    ) {

        AttachmentRow(
            selectedAttachments = attachments,
            onToggleAttachment = onToggleAttachment
        )


        Box {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,

            ) {
                IconButton(
                    onClick = openAction,
                    colors = IconButtonDefaults.iconButtonColors(
                        Color.Transparent,
                        primaryColor
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Outlined.Add,
                        tint = primaryColor,
                        contentDescription = "collapse"
                    )
                }



                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp,)
                ) {
                    DgenCursorTextfield(
                        value = textState,
                        cursorWidth = 14.dp,
                        cursorHeight = 24.dp,
                        onValueChange = { value ->
                            textState = value
                            // update multi-line state based on presence of line breaks
                            hasMultipleLines.value = value.text.contains("\n")
                        },
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        primaryColor = primaryColor,
                        cursorColor = primaryColor,
                        keyboardtype = KeyboardType.Text,
                        placeholder = {
                            androidx.compose.material3.Text(
                                text = "Type a message".uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = primaryColor.copy(pulseOpacity),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        textfieldFocusManager = focusManager,
                        dismissOnDone = false
                    )
                }


                IconButton(
                    onClick = {
                        if (textState.text.isNotBlank()) {
                            Log.d("ChatBottomAppBar", "Send button clicked. Calling onSendClick.")
                            onSendClick(textState.text)
                            /*
                            val newMsg = ChatMessage(text = textState.text)
                            // start invisible so it will animate in
                            visibleMap[newMsg.id] = mutableStateOf(false)
                            messages.add(0, newMsg)
                            input = ""
                             */
                            Log.d("ChatBottomAppBar", "onSendClick finished. Hiding keyboard and clearing text.")
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            textState = TextFieldValue()
                            expand.value = false
                        }


                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        Color.Transparent,
                        primaryColor
                    ),
                    modifier = Modifier
                        .size(56.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.sharp_send_24),
                        tint = primaryColor,
                        contentDescription = "collapse"
                    )
                }

            }
            /**/
            if(hasMultipleLines.value){
                Spacer(modifier = Modifier.width(320.dp).height(8.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(
                    dgenBlack,Color.Transparent ))))
            }


        }

    }

}

@Composable
@Preview
fun previewChatBottomAppBar() {

}