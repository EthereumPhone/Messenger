package org.ethereumhpone.chat.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.BottomNavigationDefaults.windowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Send
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.label_fontSize
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

    var isFocused by remember { mutableStateOf(true) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }


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
                .animateContentSize()
                .height(IntrinsicSize.Max)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {

                    IconButton(
                        onClick = {
                            onSendClick(textState.text)
                            focusManager.clearFocus()
                            textState = TextFieldValue()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            Color.Transparent,
                            dgenTurqoise
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "send"
                        )
                    }



                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    OldSchoolThickCursorTextField(
                        value = textState,
                        onValueChange = { value ->
                            textState = value
                            // Handle Enter key to submit command
                            /*if (value.text.contains("\n")) {
                                val newCommand = value.text.replace("\n", "")
                                commandHistory = commandHistory + newCommand
                                currentCommand = TextFieldValue("")
                            }*/
                        },
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        cursorColor = dgenWhite,
                        modifier = Modifier.weight(1f)
                    )
                }


                    IconButton(
                        onClick = {
                            onSendClick(textState.text)
                            focusManager.clearFocus()
                            textState = TextFieldValue()
                                  },
                        colors = IconButtonDefaults.iconButtonColors(
                            Color.Transparent,
                            dgenTurqoise
                        ),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "send",
                            modifier = Modifier.size(28.dp)
                        )
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