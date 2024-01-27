package org.ethereumhpone.chat.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Duo
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ethereumhpone.chat.R
import org.ethosmobile.components.library.core.calculateFontSize
import org.ethosmobile.components.library.core.ethOSTagButton
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//@Composable
//fun UserInput(){
//
//}

enum class InputSelector {
    NONE,
    EMOJI,
    WALLET,
    PICTURE
}





@Preview
@Composable
fun UserInputPreview() {{}
    UserInput(onMessageSent = {},onOpenAssetPicker = {})
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    onMessageSent: (String) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
    onOpenAssetPicker: () -> Unit
) {
    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var showActionbar by remember { mutableStateOf(false) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }

    Surface(tonalElevation = 2.dp, contentColor = Colors.WHITE, color=Colors.BLACK) {
        Column(modifier = modifier.padding(top = 8.dp, bottom = 24.dp, end = 24.dp, start = 24.dp)) {
            UserInputText(
                textFieldValue = textState,
                onTextChanged = { textState = it },
                // Only show the keyboard if there's no input selector and text field has focus
                keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                // Close extended selector if text field receives focus
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
                        resetScroll()
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState,
                onMessageSent = {
                    onMessageSent(textState.text)
                    // Reset text field and close keyboard
                    textState = TextFieldValue()
                    // Move scroll to bottom
                    resetScroll()
                    dismissKeyboard()
                } ,
                sendMessageEnabled = textState.text.isNotBlank(),
                showActionbar = showActionbar,
                onChangeShowActionBar = {
                    showActionbar = !showActionbar
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (showActionbar){
                UserInputSelector(
                    onSelectorChange = { currentInputSelector = it },
                    currentInputSelector = currentInputSelector
                )
                SelectorExpanded(
                    onCloseRequested = dismissKeyboard,
                    onTextAdded = { textState = textState.addText(it) },
                    currentSelector = currentInputSelector,
                    onOpenAssetPicker = onOpenAssetPicker
                )
            }

        }
    }
}

private fun TextFieldValue.addText(newString: String): TextFieldValue {
    val newText = this.text.replaceRange(
        this.selection.start,
        this.selection.end,
        newString
    )
    val newSelection = TextRange(
        start = newText.length,
        end = newText.length
    )

    return this.copy(text = newText, selection = newSelection)
}

@Composable
private fun SelectorExpanded(
    currentSelector: InputSelector,
    onCloseRequested: () -> Unit,
    onTextAdded: (String) -> Unit,
    onOpenAssetPicker: () -> Unit
) {
    if (currentSelector == InputSelector.NONE) return

    // Request focus to force the TextField to lose it
    val focusRequester = FocusRequester()
    // If the selector is shown, always request focus to trigger a TextField.onFocusChange.
    SideEffect {
        if (currentSelector == InputSelector.EMOJI) {
            focusRequester.requestFocus()
        }
    }

    Surface(tonalElevation = 8.dp) {
        when (currentSelector) {
            InputSelector.EMOJI -> EmojiSelector(onTextAdded, focusRequester)
            InputSelector.WALLET -> WalletSelector(focusRequester = focusRequester, onOpenAssetPicker =  onOpenAssetPicker)
            InputSelector.PICTURE -> FunctionalityNotAvailablePanel() // TODO: link to Camera
            else -> {
                throw NotImplementedError()
            }
        }
    }
}

@Composable
fun FunctionalityNotAvailablePanel() {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .height(320.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Not available",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Not available",
                modifier = Modifier.paddingFrom(FirstBaseline, before = 32.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UserInputSelector(
    onSelectorChange: (InputSelector) -> Unit,
    currentInputSelector: InputSelector,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .height(72.dp)
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Row {
            InputSelectorButton(
                onClick = { onSelectorChange(InputSelector.EMOJI) },
                icon = Icons.Outlined.Mood,
                selected = currentInputSelector == InputSelector.EMOJI,
                description = stringResource(id = R.string.emoji_selector_bt_desc)
            )
            InputSelectorButton(
                onClick = { onSelectorChange(InputSelector.WALLET) },
                icon = ImageVector.vectorResource(R.drawable.wallet),
                selected = currentInputSelector == InputSelector.WALLET,
                description = stringResource(id = R.string.wallet)
            )
            InputSelectorButton(
                onClick = { onSelectorChange(InputSelector.PICTURE) },
                icon = Icons.Outlined.InsertPhoto,
                selected = currentInputSelector == InputSelector.PICTURE,
                description = stringResource(id = R.string.attach_photo_desc)
            )

        }


    }
}

@Composable
private fun InputSelectorButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            color = Colors.DARK_GRAY,
            shape = RoundedCornerShape(14.dp)
        )
    } else {
        Modifier
    }
    IconButton(
        onClick = onClick,
        modifier = modifier.then(backgroundModifier)
    ) {
        val tint = if (selected) {
            contentColorFor(backgroundColor = LocalContentColor.current)
        } else {
            LocalContentColor.current
        }
        Icon(
            icon,
            tint = tint,
            modifier = Modifier
                .padding(8.dp)
                .size(56.dp),
            contentDescription = description
        )
    }
}

@Composable
private fun NotAvailablePopup(onDismissed: () -> Unit) {
    FunctionalityNotAvailablePopup(onDismissed)
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
    onMessageSent: () -> Unit,
    sendMessageEnabled: Boolean,
    showActionbar: Boolean,
    onChangeShowActionBar:  () -> Unit
) {
    val swipeOffset = remember { mutableStateOf(0f) }
    var isRecordingMessage by remember { mutableStateOf(false) }
//    var isRecordingMessage by remember { mutableStateOf(false) }
    val a11ylabel = stringResource(id = R.string.textfield_desc)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
           ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        var startAnimation by remember { mutableStateOf(false) }
        val animationSpec = tween<Float>(durationMillis = 400, easing = LinearOutSlowInEasing)
        val rotationAngle by animateFloatAsState(
            targetValue = if (startAnimation) 45f else 0f,
            animationSpec = animationSpec,

            )

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .size(42.dp)
                ,
            enabled = true,
            onClick = {
                onChangeShowActionBar()
                startAnimation = !startAnimation
            },
        ) {
            Box(
                contentAlignment = Alignment.Center
            ){
                Icon(imageVector = Icons.Filled.Add, modifier= Modifier
                    .size(32.dp)
                    .graphicsLayer(rotationZ = rotationAngle),contentDescription = "Send",tint = Color.White)
            }

        }
        Spacer(modifier = Modifier.width(8.dp))

        AnimatedContent(
            targetState = isRecordingMessage,
            label = "text-field",
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) { recording ->
            Box(
                Modifier
                    .fillMaxSize()
                    .border(1.dp, color = Colors.DARK_GRAY, shape = CircleShape)
            ) {
                //TODO: Recording
                if (recording) {
                    RecordingIndicator { swipeOffset.value }
                } else {
                    UserInputTextField(
                        textFieldValue,
                        onTextChanged,
                        onTextFieldFocused,
                        keyboardType,
                        focusState,
                        Modifier.semantics {
                            contentDescription = a11ylabel
                            keyboardShownProperty = keyboardShown
                        },
                        onMessageSent,
                        sendMessageEnabled
                    )
                }
            }
        }

        if(sendMessageEnabled){ Color(0xFF8C7DF7)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF8C7DF7))
                    .size(42.dp),
                enabled = true,
                onClick = onMessageSent,
            ) {
                Icon(imageVector = Icons.Rounded.ArrowUpward,modifier= Modifier
                    .size(32.dp), contentDescription = "Send",tint = Color.White)
            }
        }


    }
}

@Composable
private fun BoxScope.UserInputTextField(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    keyboardType: KeyboardType,
    focusState: Boolean,
    modifier: Modifier = Modifier,
    onMessageSent: () -> Unit,
    sendMessageEnabled: Boolean,
) {
    var lastFocusState by remember { mutableStateOf(false) }
    BasicTextField(
        value = textFieldValue,
        onValueChange = { onTextChanged(it) },
        modifier = modifier
            .align(Alignment.CenterStart)
            .onFocusChanged { state ->
                if (lastFocusState != state.isFocused) {
                    onTextFieldFocused(state.isFocused)
                }
                lastFocusState = state.isFocused
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Send
        ),

        cursorBrush = SolidColor(
            Colors.WHITE
        ),
        textStyle = TextStyle(
            fontWeight = FontWeight.Medium,
            fontFamily = Fonts.INTER,
            fontSize = 16.sp,
            color = Colors.WHITE,
        )
    )
    { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            innerTextField()
            // Send button



        }
    }

    val disableContentColor =
        MaterialTheme.colorScheme.onSurfaceVariant
    if (textFieldValue.text.isEmpty() && !focusState) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp),
            text = stringResource(R.string.textfield_hint),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Medium,
            fontFamily = Fonts.INTER,
            fontSize = 16.sp,
            color = Colors.GRAY,
        )
    }



}

@Composable
private fun RecordingIndicator(swipeOffset: () -> Float) {
    var duration by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            duration += 1.seconds
        }
    }
    Row(
        Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")

        val animatedPulse = infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse",
        )
        Box(
            Modifier
                .size(56.dp)
                .padding(24.dp)
                .graphicsLayer {
                    scaleX = animatedPulse.value; scaleY = animatedPulse.value
                }
                .clip(CircleShape)
                .background(Color.Red)
        )
        Text(
            duration.toComponents { minutes, seconds, _ ->
                val min = minutes.toString().padStart(2, '0')
                val sec = seconds.toString().padStart(2, '0')
                "$min:$sec"
            },
            Modifier.alignByBaseline()
        )
        Box(
            Modifier
                .fillMaxSize()
                .alignByBaseline()
                .clipToBounds()
        ) {
            val swipeThreshold = with(LocalDensity.current) { 200.dp.toPx() }
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = swipeOffset() / 2
                        alpha = 1 - (swipeOffset().absoluteValue / swipeThreshold)
                    },
                textAlign = TextAlign.Center,
                text = stringResource(R.string.swipe_to_cancel_recording),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EmojiSelector(
    onTextAdded: (String) -> Unit,
    focusRequester: FocusRequester
) {

    val scrollState = rememberScrollState()
    val a11yLabel = stringResource(id = R.string.emoji_selector_desc)
    Column(
        modifier = Modifier
            .focusRequester(focusRequester) // Requests focus when the Emoji selector is displayed
            // Make the emoji selector focusable so it can steal focus from TextField
            .focusTarget()
            .background(Colors.BLACK)
            .semantics { contentDescription = a11yLabel }
    ) {
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            EmojiTable(onTextAdded, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun WalletSelector(
    focusRequester: FocusRequester,
    onOpenAssetPicker: () -> Unit
) {

    Column(
        modifier = Modifier
            .focusRequester(focusRequester) // Requests focus when the Emoji selector is displayed
            // Make the emoji selector focusable so it can steal focus from TextField
            .focusTarget()
            .background(Colors.BLACK)
            .semantics { contentDescription = "Send ETH" }
    ) {
        Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
            WalletTable(
                tokenBalance = 0.0,
                modifier = Modifier.padding(8.dp),
                onOpenAssetPicker = onOpenAssetPicker
            )
        }
    }
}

@Composable
fun ExtendedSelectorInnerButton(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        else Color.Transparent,
        disabledContainerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
    )
    TextButton(
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .height(36.dp),
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        repeat(4) { x ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(EMOJI_COLUMNS) { y ->
                    val emoji = emojis[x * EMOJI_COLUMNS + y]
                    Text(
                        modifier = Modifier
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 42.dp, minHeight = 42.dp)
                            .padding(8.dp),
                        text = emoji,
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WalletTable(
    tokenBalance: Double,
    onOpenAssetPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    val token by remember { mutableStateOf("ETH") }


        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ){
                Box ( modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Colors.GRAY)){}
                Text(
                    text = "Mainnet",
                    fontSize = 12.sp,
                    color = Colors.GRAY,
                    fontWeight = FontWeight.Normal
                )
            }

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ){
                ethOSTextField(
                    modifier = Modifier
                        .background(Color.Red)
                        .weight(1f),
                    text = amount,
                    label = "0",
                    singleLine = true,
                    onTextChanged = { text ->
                        if (text.isEmpty() || text == "." || text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                            // If it's a valid format or empty, call onAmountChange with the text
                            amount = text
                        }
                    },
                    size = 64,
                    maxChar = 10,
                    color = if(amount.isNotEmpty() && amount!="." && amount!=",") {
                        if((amount.toDoubleOrNull() ?: 0.0) < tokenBalance){
                            Colors.WHITE
                        }else{
                            Colors.ERROR
                        }
                    } else {
                        Colors.WHITE
                    },
                    numberInput = true
                )

                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF8C7DF7))
                        .size(56.dp)
                        ,
                    enabled = true,
                    onClick = {}, //sent ETH
                ) {
                    Icon(imageVector = Icons.Rounded.ArrowUpward, modifier = Modifier.size(36.dp), contentDescription = "Send",tint = Colors.WHITE)
                }
            }

            Text(
                text = "${formatDouble(tokenBalance)} available",
                fontSize = 20.sp,
                color = Colors.GRAY,
                fontWeight = FontWeight.Normal
            )

            ethOSTagButton(text = token) {
                onOpenAssetPicker() //Open Asset Modal Sheet
            }
        }








}


@Composable
fun ethOSTextField(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = false,//true,
    maxChar: Int = 42,
    sizeCut: Int = 2,
    numberInput: Boolean = false,
    onTextChanged: (String) -> Unit,
    color: Color = Color.White
) {

    var isFocused by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(size.sp) }


    BasicTextField(
        value = text,
        onValueChange = {

            if(it.length < maxChar){
                onTextChanged(it)
            }



            fontSize = size.sp

        },
        keyboardOptions = if(numberInput) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = singleLine,
        minLines = 1,
        maxLines = 2,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = Fonts.INTER,
            color = color,
            fontSize = calculateFontSize(text.length,size,sizeCut),
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.width(IntrinsicSize.Min),
        cursorBrush = SolidColor(Color.White),



        ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (!isFocused && text.isEmpty()) {
                Text(
                    text = label,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Fonts.INTER,
                    fontSize = size.sp,
                    color = Colors.GRAY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            innerTextField()
        }
    }


}

private const val EMOJI_COLUMNS = 10

private val emojis = listOf(
    "\ud83d\ude00", // Grinning Face
    "\ud83d\ude01", // Grinning Face With Smiling Eyes
    "\ud83d\ude02", // Face With Tears of Joy
    "\ud83d\ude03", // Smiling Face With Open Mouth
    "\ud83d\ude04", // Smiling Face With Open Mouth and Smiling Eyes
    "\ud83d\ude05", // Smiling Face With Open Mouth and Cold Sweat
    "\ud83d\ude06", // Smiling Face With Open Mouth and Tightly-Closed Eyes
    "\ud83d\ude09", // Winking Face
    "\ud83d\ude0a", // Smiling Face With Smiling Eyes
    "\ud83d\ude0b", // Face Savouring Delicious Food
    "\ud83d\ude0e", // Smiling Face With Sunglasses
    "\ud83d\ude0d", // Smiling Face With Heart-Shaped Eyes
    "\ud83d\ude18", // Face Throwing a Kiss
    "\ud83d\ude17", // Kissing Face
    "\ud83d\ude19", // Kissing Face With Smiling Eyes
    "\ud83d\ude1a", // Kissing Face With Closed Eyes
    "\u263a", // White Smiling Face
    "\ud83d\ude42", // Slightly Smiling Face
    "\ud83e\udd17", // Hugging Face
    "\ud83d\ude07", // Smiling Face With Halo
    "\ud83e\udd13", // Nerd Face
    "\ud83e\udd14", // Thinking Face
    "\ud83d\ude10", // Neutral Face
    "\ud83d\ude11", // Expressionless Face
    "\ud83d\ude36", // Face Without Mouth
    "\ud83d\ude44", // Face With Rolling Eyes
    "\ud83d\ude0f", // Smirking Face
    "\ud83d\ude23", // Persevering Face
    "\ud83d\ude25", // Disappointed but Relieved Face
    "\ud83d\ude2e", // Face With Open Mouth
    "\ud83e\udd10", // Zipper-Mouth Face
    "\ud83d\ude2f", // Hushed Face
    "\ud83d\ude2a", // Sleepy Face
    "\ud83d\ude2b", // Tired Face
    "\ud83d\ude34", // Sleeping Face
    "\ud83d\ude0c", // Relieved Face
    "\ud83d\ude1b", // Face With Stuck-Out Tongue
    "\ud83d\ude1c", // Face With Stuck-Out Tongue and Winking Eye
    "\ud83d\ude1d", // Face With Stuck-Out Tongue and Tightly-Closed Eyes
    "\ud83d\ude12", // Unamused Face
    "\ud83d\ude13", // Face With Cold Sweat
    "\ud83d\ude14", // Pensive Face
    "\ud83d\ude15", // Confused Face
    "\ud83d\ude43", // Upside-Down Face
    "\ud83e\udd11", // Money-Mouth Face
    "\ud83d\ude32", // Astonished Face
    "\ud83d\ude37", // Face With Medical Mask
    "\ud83e\udd12", // Face With Thermometer
    "\ud83e\udd15", // Face With Head-Bandage
    "\u2639", // White Frowning Face
    "\ud83d\ude41", // Slightly Frowning Face
    "\ud83d\ude16", // Confounded Face
    "\ud83d\ude1e", // Disappointed Face
    "\ud83d\ude1f", // Worried Face
    "\ud83d\ude24", // Face With Look of Triumph
    "\ud83d\ude22", // Crying Face
    "\ud83d\ude2d", // Loudly Crying Face
    "\ud83d\ude26", // Frowning Face With Open Mouth
    "\ud83d\ude27", // Anguished Face
    "\ud83d\ude28", // Fearful Face
    "\ud83d\ude29", // Weary Face
    "\ud83d\ude2c", // Grimacing Face
    "\ud83d\ude30", // Face With Open Mouth and Cold Sweat
    "\ud83d\ude31", // Face Screaming in Fear
    "\ud83d\ude33", // Flushed Face
    "\ud83d\ude35", // Dizzy Face
    "\ud83d\ude21", // Pouting Face
    "\ud83d\ude20", // Angry Face
    "\ud83d\ude08", // Smiling Face With Horns
    "\ud83d\udc7f", // Imp
    "\ud83d\udc79", // Japanese Ogre
    "\ud83d\udc7a", // Japanese Goblin
    "\ud83d\udc80", // Skull
    "\ud83d\udc7b", // Ghost
    "\ud83d\udc7d", // Extraterrestrial Alien
    "\ud83e\udd16", // Robot Face
    "\ud83d\udca9", // Pile of Poo
    "\ud83d\ude3a", // Smiling Cat Face With Open Mouth
    "\ud83d\ude38", // Grinning Cat Face With Smiling Eyes
    "\ud83d\ude39", // Cat Face With Tears of Joy
    "\ud83d\ude3b", // Smiling Cat Face With Heart-Shaped Eyes
    "\ud83d\ude3c", // Cat Face With Wry Smile
    "\ud83d\ude3d", // Kissing Cat Face With Closed Eyes
    "\ud83d\ude40", // Weary Cat Face
    "\ud83d\ude3f", // Crying Cat Face
    "\ud83d\ude3e", // Pouting Cat Face
    "\ud83d\udc66", // Boy
    "\ud83d\udc67", // Girl
    "\ud83d\udc68", // Man
    "\ud83d\udc69", // Woman
    "\ud83d\udc74", // Older Man
    "\ud83d\udc75", // Older Woman
    "\ud83d\udc76", // Baby
    "\ud83d\udc71", // Person With Blond Hair
    "\ud83d\udc6e", // Police Officer
    "\ud83d\udc72", // Man With Gua Pi Mao
    "\ud83d\udc73", // Man With Turban
    "\ud83d\udc77", // Construction Worker
    "\u26d1", // Helmet With White Cross
    "\ud83d\udc78", // Princess
    "\ud83d\udc82", // Guardsman
    "\ud83d\udd75", // Sleuth or Spy
    "\ud83c\udf85", // Father Christmas
    "\ud83d\udc70", // Bride With Veil
    "\ud83d\udc7c", // Baby Angel
    "\ud83d\udc86", // Face Massage
    "\ud83d\udc87", // Haircut
    "\ud83d\ude4d", // Person Frowning
    "\ud83d\ude4e", // Person With Pouting Face
    "\ud83d\ude45", // Face With No Good Gesture
    "\ud83d\ude46", // Face With OK Gesture
    "\ud83d\udc81", // Information Desk Person
    "\ud83d\ude4b", // Happy Person Raising One Hand
    "\ud83d\ude47", // Person Bowing Deeply
    "\ud83d\ude4c", // Person Raising Both Hands in Celebration
    "\ud83d\ude4f", // Person With Folded Hands
    "\ud83d\udde3", // Speaking Head in Silhouette
    "\ud83d\udc64", // Bust in Silhouette
    "\ud83d\udc65", // Busts in Silhouette
    "\ud83d\udeb6", // Pedestrian
    "\ud83c\udfc3", // Runner
    "\ud83d\udc6f", // Woman With Bunny Ears
    "\ud83d\udc83", // Dancer
    "\ud83d\udd74", // Man in Business Suit Levitating
    "\ud83d\udc6b", // Man and Woman Holding Hands
    "\ud83d\udc6c", // Two Men Holding Hands
    "\ud83d\udc6d", // Two Women Holding Hands
    "\ud83d\udc8f" // Kiss
)

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}


//@Composable
//fun TextInput(
//    text: String,
//    size: Int,
//    modifier: Modifier = Modifier,
//    label: String = "",
//    singleLine: Boolean = false,//true,
//    maxChar: Int = 42,
//    sizeCut: Int = 2,
//    numberInput: Boolean = false,
//    onTextChanged: (String) -> Unit,
//    color: Color = Color.White
//){
//    BasicTextField(
//        value = text,
//        onValueChange = {
//
//            onTextChanged(it)
//
//
//
//        },
//        keyboardOptions = if(numberInput) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions(keyboardType = KeyboardType.Text),
//        singleLine = singleLine,
//        minLines = 1,
//        maxLines = 2,
//        textStyle = LocalTextStyle.current.copy(
//            fontFamily = Fonts.INTER,
//            color = color,
//            fontSize = calculateFontSize(text.length,size,sizeCut),
//            fontWeight = FontWeight.SemiBold
//        ),
//        modifier = Modifier.width(IntrinsicSize.Min),
//        cursorBrush = SolidColor(Color.White),
//
//
//
//        ) { innerTextField ->
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center,
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            if (!isFocused && text.isEmpty()) {
//                Text(
//                    text = label,
//                    textAlign = TextAlign.Start,
//                    fontWeight = FontWeight.Medium,
//                    fontFamily = Fonts.INTER,
//                    fontSize = size.sp,
//                    color = Colors.GRAY,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//            innerTextField()
//        }
//    }
//}

@Composable
fun FunctionalityNotAvailablePopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = "Functionality not available \uD83D\uDE48",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE")
            }
        }
    )
}

