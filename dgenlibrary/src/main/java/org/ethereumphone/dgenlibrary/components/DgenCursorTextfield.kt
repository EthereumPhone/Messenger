package org.ethereumphone.dgenlibrary.components

import android.R.attr.enabled
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.geometry.Rect
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.util.CustomTextSelectionMenuContent
import com.example.dgenlibrary.util.CustomTextToolbar
import com.example.dgenlibrary.util.CustomTextToolbarState
import org.ethereumphone.dgenlibrary.theme.dgenGray

@Composable
fun DgenCursorTextfield(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = dgenGray,
    keyboardtype: KeyboardType =  KeyboardType.Text,
    placeholder: @Composable () -> Unit = {
        Text(
            text = "Type a message",
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = primaryColor.copy(alpha = 0.45f),
                fontWeight = FontWeight.SemiBold,
                fontSize = body2_fontSize
            )
        )
    },
    contentAlignment: Alignment = Alignment.CenterStart,
    textStyle: TextStyle = LocalTextStyle.current,
    cursorColor: Color = MaterialTheme.colors.primary,
    cursorWidth: Dp = 18.dp,
    cursorHeight: Dp = 32.dp,
    blinkDuration: Int = 500,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxFieldHeight: Dp = 150.dp,
    cursorVerticalOffset: Dp = 24.dp,
    singleLine: Boolean = false,
    textfieldFocusManager: FocusManager? = null,
    dismissOnDone: Boolean = true,
    imeAction: ImeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
) {

    // 1) blink animation, layout & focus state
    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(blinkDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isFocused by remember { mutableStateOf(false) }

    // 2) scroll state for vertical scrolling
    val scrollState = rememberScrollState()

    // 3) auto‐scroll down whenever new text bumps max scroll
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }



    val focusManager = textfieldFocusManager ?: LocalFocusManager.current

    // Custom text toolbar for selection menu
    val textToolbarState = remember { CustomTextToolbarState(primaryColor) }
    val customTextToolbar = remember(textToolbarState) { CustomTextToolbar(textToolbarState) }

    Box(
        modifier = Modifier.heightIn(max = maxFieldHeight)      // fix the height so overflow can happen
            .verticalScroll(scrollState),
        contentAlignment = contentAlignment
    ) {

        if (value.text.isBlank()) {
            if (placeholder != null && !isFocused) {
                placeholder()
            }
        }

        // Custom selection colors with transparent handles
        val customTextSelectionColors = TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = primaryColor.copy(alpha = 0.4f)
        )

        CompositionLocalProvider(
            LocalTextSelectionColors provides customTextSelectionColors,
            LocalTextToolbar provides customTextToolbar
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (isFocused){
                            textLayoutResult
                                ?.takeIf { value.selection.collapsed }
                                ?.let { tlr ->
                                    val rect = tlr.getCursorRect(value.selection.start)

                                    // subtract scroll to bring into view
                                    //val y = rect.top + (rect.height - 32.dp.toPx()) / 2f + 48.dp.toPx()
                                    val y = ((rect.top + rect.bottom) / 2 ) - (cursorHeight.toPx() /2)
                                    drawRect(
                                        color   = cursorColor.copy(alpha = if (isFocused) blinkAlpha else 0f),
                                        topLeft = Offset(rect.left, y),
                                        size    = Size(cursorWidth.toPx(), cursorHeight.toPx())
                                    )
                                }
                            /*textLayoutResult?.let {
                                val cursorRect = it.getCursorRect(value.selection.start)
                                drawRect(
                                    color = cursorColor,
                                    topLeft = Offset(cursorRect.left, ((cursorRect.top + cursorRect.bottom) / 2 ) - (cursorHeight.toPx() /2)),
                                    size = androidx.compose.ui.geometry.Size(cursorWidth.toPx(), cursorHeight.toPx()),
                                    alpha = blinkAlpha
                                )
                            }*/
                        }

                    }
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    }
                ,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = imeAction,
                    keyboardType = keyboardtype
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        isFocused = true
                    },
                    onDone = {
                        if (dismissOnDone && imeAction == ImeAction.Done) {
                            focusManager.clearFocus() // remove focus only when dismissal desired
                            isFocused = false
                        }
                    }
                ),
                textStyle = textStyle,
                visualTransformation = VisualTransformation.None, // Ensure no transformations
                cursorBrush = SolidColor(Color.Unspecified),
                onTextLayout = { textLayoutResult = it },
                singleLine = singleLine
            )
        }
        /*BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            visualTransformation = visualTransformation,
            modifier = modifier.fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }.fillMaxWidth().background(Color.Blue.copy(0.1f)).heightIn(min=50.dp),
            cursorBrush = SolidColor(Color.Transparent),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = keyboardtype
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    isFocused = true
                },
                onDone = {
                    focusManager.clearFocus() // Fokus entfernen, wenn Enter gedrückt wird
                    isFocused = false
                }
            ),

        ) { innerTextField ->
            // 4) draw text + custom cursor, offset by scrollState.value
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        textLayoutResult
                            ?.takeIf { value.selection.collapsed }
                            ?.let { tlr ->
                                val rect = tlr.getCursorRect(value.selection.start)
                                // subtract scroll to bring into view
                                val y = rect.top
                                - scrollState.value
                                + (rect.height - cursorHeight) / 2f
                                + cursorVerticalOffset.toPx()

                                drawRect(
                                    color   = cursorColor.copy(alpha = if (isFocused) blinkAlpha else 0f),
                                    topLeft = Offset(rect.left, y),
                                    size    = Size(cursorWidth, cursorHeight)
                                )
                            }
                    }
            ) {
                innerTextField()
            }
        }*/

    }

    // Render custom selection menu
    CustomTextSelectionMenuContent(textToolbarState)
}

