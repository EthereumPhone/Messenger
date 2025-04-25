package org.ethereumhpone.chat.components



import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar

@Composable
fun OldSchoolThickCursorTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {
        Text(
            text = "Type a message",
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenTurqoise.copy(alpha = 0.45f),
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp
            )
        )
    },
    textStyle: TextStyle = LocalTextStyle.current,
    cursorColor: Color = MaterialTheme.colors.primary,
    cursorWidth: Float = 16f,
    cursorHeight: Float = 32f,
    blinkDuration: Int = 500,
    hasMultipleLines: MutableState<Boolean> = mutableStateOf(false),
    expand: MutableState<Boolean> = mutableStateOf(false),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxFieldHeight: Dp = 150.dp,
    cursorVerticalOffset: Dp = 18.dp,
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

    var lineCount by remember { mutableStateOf(1)}

    Box(
        modifier = modifier.heightIn(max = maxFieldHeight)      // fix the height so overflow can happen
            .verticalScroll(scrollState),
        contentAlignment = Alignment.CenterStart
    ) {

        if (value.text.isBlank()) {
            placeholder()
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            visualTransformation = visualTransformation,
            modifier = modifier
                .onFocusChanged { isFocused = it.isFocused },
            cursorBrush = SolidColor(Color.Transparent),
            onTextLayout = { layoutResult ->
                lineCount = layoutResult.lineCount
                hasMultipleLines.value = (lineCount > 1)
                textLayoutResult = layoutResult

                if(hasMultipleLines.value && lineCount > 3){
                    expand.value = true
                }
            }
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
        }

    }
}



// Helper class to handle text layout consistently
private class TextDelegate(
    val text: String,
    val style: TextStyle,
    val maxWidth: Float,
    val density: Density
) {
    // Add methods as needed for complex cursor positioning
}

// For convenience, also provide a String-based version
@Composable
fun OldSchoolThickCursorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    cursorColor: Color = MaterialTheme.colors.primary,
    cursorWidth: Float = 8f,
    cursorHeight: Float = 24f,
    blinkDuration: Int = 500,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value))
    }

    OldSchoolThickCursorTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            if (it.text != value) {
                onValueChange(it.text)
            }
        },
        modifier = modifier,
        textStyle = textStyle,
        cursorColor = cursorColor,
        cursorWidth = cursorWidth,
        cursorHeight = cursorHeight,
        blinkDuration = blinkDuration,
        visualTransformation = visualTransformation
    )
}

// Demo for multiple lines of text
@Composable
fun MultiLineTest() {
    var text by remember { mutableStateOf(
        """This is line 1
        |This is line 2
        |This is line 3
        |This is line 4
        |This is line 5
        |This is a very long line that should wrap to the next line when it reaches the end
        """.trimMargin()
    ) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        OldSchoolThickCursorTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(
                color = Color.Green,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            ),
            cursorColor = Color.Green,
            cursorWidth = 8f,
            cursorHeight = 18f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}





@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 400,
    name = "Old School Terminal Preview with TextFieldValue"
)
@Composable
fun OldSchoolThickCursorPreview() {
    MaterialTheme {
        Surface(
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Old School Terminal",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Green terminal (with text)
                TerminalPreviewSection(
                    title = "C:\\> DIR",
                    textColor = Color(0xFF00FF00), // Bright green
                    initialText = "AUTOEXEC.BAT CONFIG.SYS",
                    backgroundColor = Color(0xFF001100)
                )

                // Amber terminal (empty)
                TerminalPreviewSection(
                    title = "C:\\> _",
                    textColor = Color(0xFFFFB700), // Amber
                    initialText = "",
                    backgroundColor = Color(0xFF332200)
                )

                // Blue terminal
                TerminalPreviewSection(
                    title = "READY.",
                    textColor = Color(0xFF00AAFF), // Blue
                    initialText = "10 PRINT \"HELLO WORLD\"",
                    backgroundColor = Color(0xFF001133)
                )

                // White terminal
                TerminalPreviewSection(
                    title = "login:",
                    textColor = Color.White,
                    initialText = "admin",
                    backgroundColor = Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
private fun TerminalPreviewSection(
    title: String,
    textColor: Color,
    initialText: String,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        var textFieldValue by remember {
            mutableStateOf(TextFieldValue(initialText))
        }

        OldSchoolThickCursorTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            textStyle = TextStyle(
                color = textColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp
            ),
            cursorColor = textColor,
            cursorWidth = 10f,
            cursorHeight = 20f,
            blinkDuration = 600,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 300,
    heightDp = 300,
    name = "Interactive Terminal"
)
@Composable
fun OldSchoolTerminalInteractivePreview() {
    var commandHistory by remember {
        mutableStateOf(listOf<String>())
    }

    var currentCommand by remember {
        mutableStateOf(TextFieldValue(""))
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            Column {
                // Display command history
                commandHistory.forEach { command ->
                    Row {
                        Text(
                            text = "> ",
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = command,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Input field with prompt
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )

                    OldSchoolThickCursorTextField(
                        value = currentCommand,
                        onValueChange = { value ->
                            currentCommand = value
                            // Handle Enter key to submit command
                            if (value.text.contains("\n")) {
                                val newCommand = value.text.replace("\n", "")
                                commandHistory = commandHistory + newCommand
                                currentCommand = TextFieldValue("")
                            }
                        },
                        textStyle = TextStyle(
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        ),
                        cursorColor = Color.Green,
                        cursorWidth = 12f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/*
CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    /*TextField(
                        shape = RoundedCornerShape(35.dp),
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntSize.VisibilityThreshold
                            ))
                            .drawBehind {
                                if (isFocused){
                                    textLayoutResult?.let {
                                        val cursorRect = it.getCursorRect(textState.selection.start)
                                        drawRect(
                                            color = dgenTurqoise,
                                            topLeft = Offset(cursorRect.left, ((cursorRect.top + cursorRect.bottom) / 2 ) - (32.dp.toPx() /2)),
                                            size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 32.dp.toPx()),
                                            alpha = cursorAlpha
                                        )
                                    }
                                }
                            }
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            }
                        ,
                        keyboardActions = KeyboardActions(
                            onGo = {
                                isFocused = false
                            },
                            onDone = {
                                focusManager.clearFocus() // Fokus entfernen, wenn Enter gedrückt wird
                                isFocused = false
                            }
                        ),
                        placeholder = {
                            Text(
                                text = "Type a message",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = dgenWhite,
                            unfocusedTextColor = dgenWhite,
                            focusedContainerColor = Colors.TRANSPARENT,
                            unfocusedContainerColor = Colors.TRANSPARENT,
                            disabledContainerColor = Colors.TRANSPARENT,
                            cursorColor = dgenWhite,
                            errorCursorColor = dgenWhite,
                            focusedBorderColor = Colors.TRANSPARENT,
                            unfocusedBorderColor = Colors.TRANSPARENT,
                            focusedPlaceholderColor = dgenWhite.copy(0.7f),
                            unfocusedPlaceholderColor = dgenWhite.copy(0.7f),
                        ),
                        textStyle = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                    )*/
                    BasicTextField(
                        value = textState,
                        onValueChange = {  textState = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            }
                        ,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Start,
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.Normal,
                            fontSize = label_fontSize,
                            lineHeight = label_fontSize,
                            letterSpacing = 0.5.sp,
                            textDecoration = TextDecoration.None,
                        ),
                        visualTransformation = VisualTransformation.None, // Ensure no transformations
                        cursorBrush = SolidColor(Color.Unspecified),
                        minLines = 1,
                        maxLines = 4,
                        onTextLayout = { textLayoutResult = it }
                    )
                }

 */