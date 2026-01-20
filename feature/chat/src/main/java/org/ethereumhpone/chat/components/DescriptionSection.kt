package org.ethereumhpone.chat.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.DgenBasicTextfield
import org.ethereumphone.dgenlibrary.theme.dgenWhite

/**
 * DescriptionSection - A reusable component for description/note input.
 * Based on RecipientSection structure but adapted for general text input.
 * 
 * @param description Current description text
 * @param onDescriptionChange Callback when description changes
 * @param title Header title (defaults to "DESCRIPTION")
 * @param placeholder Placeholder text
 * @param primaryColor Primary color for styling
 * @param maxLines Maximum number of lines for the text field
 * @param maxLength Maximum character length (optional)
 * @param shouldDismissKeyboard Whether to dismiss keyboard
 * @param onKeyboardDismissed Callback when keyboard is dismissed
 */
@Composable
fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    title: String = "DESCRIPTION",
    placeholder: String = "What is this for? (optional)",
    primaryColor: Color = SystemColorManager.primaryColor,
    maxLines: Int = 2,
    maxLength: Int? = 100,
    shouldDismissKeyboard: Boolean = false,
    onKeyboardDismissed: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val isAnyFieldFocused = remember { mutableStateOf(false) }
    
    // Keep cursor position
    var textFieldValue by remember { mutableStateOf(TextFieldValue(description)) }
    var isFocused by remember { mutableStateOf(false) }
    
    // Animated background opacity for fading effect
    val animatedBackgroundOpacity by animateFloatAsState(
        targetValue = if (isFocused) 0.08f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundOpacity"
    )
    
    LaunchedEffect(description) {
        if (textFieldValue.text != description) {
            textFieldValue = TextFieldValue(
                text = description,
                selection = TextRange(description.length)
            )
        }
    }
    
    // Clear keyboard when requested
    LaunchedEffect(shouldDismissKeyboard) {
        if (shouldDismissKeyboard) {
            focusManager.clearFocus()
            onKeyboardDismissed()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val overflowH = 12.dp.toPx()
                val overflowV = 8.dp.toPx()
                drawRoundRect(
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    color = primaryColor,
                    size = Size(size.width + overflowH * 2, size.height + overflowV * 2),
                    topLeft = Offset(-overflowH, -overflowV),
                    alpha = animatedBackgroundOpacity
                )
            }
    ) {
        // Header title
        Text(
            text = title,
            color = primaryColor,
            style = TextStyle(
                fontFamily = SpaceMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = label_fontSize,
                lineHeight = label_fontSize,
                letterSpacing = 1.sp,
                textDecoration = TextDecoration.None,
                textAlign = TextAlign.Left
            )
        )
        
        // Text field
        DgenBasicTextfield(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = textFieldValue,
            onValueChange = { new ->
                // Apply max length if specified
                val newText = if (maxLength != null && new.text.length > maxLength) {
                    new.copy(text = new.text.take(maxLength))
                } else {
                    new
                }
                textFieldValue = newText
                onDescriptionChange(newText.text)
            },
            maxLines = maxLines,
            cursorColor = primaryColor,
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite.copy(alpha = pulseOpacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Start
                    )
                )
            },
            textStyle = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 25.sp,
                textAlign = TextAlign.Start
            ),
            keyboardtype = KeyboardType.Text,
            cursorWidth = 16.dp,
            cursorHeight = 16.dp,
            isAnyFieldFocused = isAnyFieldFocused,
            scrollHorizontally = false,
            onFocusChanged = { focusState ->
                isFocused = focusState
            }
        )
    }
}
