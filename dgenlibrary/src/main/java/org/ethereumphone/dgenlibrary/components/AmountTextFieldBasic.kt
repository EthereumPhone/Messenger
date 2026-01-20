package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
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
import com.example.dgenlibrary.ui.theme.pulseOpacity
import com.example.dgenlibrary.ui.theme.smallDuration
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.theme.dgenWhite

/**
 * A styled amount input text field component that matches the WalletManager/TokenLauncher design.
 * Features a vertical accent bar, title, max amount display, and a slot for secondary content (e.g. token selector).
 * 
 * @param currentAmount The current amount displayed in the text field
 * @param currentFiatAmount The current fiat equivalent amount
 * @param formattedMaxAmount The formatted max amount available (e.g., "1.234")
 * @param formattedMaxFiatAmount The formatted max fiat amount available (e.g., "$1,234.00")
 * @param useMaxAmount Whether the max amount is currently active
 * @param title The title displayed above the input (e.g., "FROM", "TO", "AMOUNT")
 * @param secondaryContent Composable slot for secondary content like token selector
 * @param onAmountChange Callback when the amount changes, includes isFiat flag
 * @param onMaxClick Callback when max button/title is clicked
 * @param readOnly Whether the text field is read-only
 * @param showMaxAmount Whether to show "MAX" prefix in the max amount label
 * @param maxClickable Whether the max amount is clickable
 * @param secondarySelectable Whether the secondary content slot should indicate selectability
 * @param primaryColor Override the primary color (defaults to SystemColorManager.primaryColor)
 */
@Composable
fun AmountTextFieldBasic(
    currentAmount: String,
    currentFiatAmount: String,
    formattedMaxAmount: String,
    formattedMaxFiatAmount: String,
    useMaxAmount: Boolean,
    title: String,
    secondaryContent: @Composable (Boolean) -> Unit,
    onAmountChange: (String, Boolean) -> Unit,
    onMaxClick: () -> Unit,
    readOnly: Boolean = false,
    showMaxAmount: Boolean = true,
    maxClickable: Boolean = !readOnly,
    secondarySelectable: Boolean = !readOnly,
    primaryColor: Color = SystemColorManager.primaryColor
) {
    var toggleFiat by remember { mutableStateOf(false) }

    val maxAlpha by animateFloatAsState(
        targetValue = if (useMaxAmount && maxClickable) 1f else pulseOpacity,
        animationSpec = tween(smallDuration, easing = FastOutLinearInEasing),
        label = "maxAlpha"
    )

    val amount = if (toggleFiat) {
        currentFiatAmount
    } else {
        currentAmount
    }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(amount)) }
    LaunchedEffect(amount) {
        if (textFieldValue.text != amount) {
            textFieldValue = TextFieldValue(
                text = amount,
                selection = TextRange(amount.length)
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vertical accent bar
        Spacer(
            Modifier
                .offset(y = 3.dp)
                .height(77.dp)
                .width(8.dp)
                .background(primaryColor.copy(pulseOpacity))
                .padding(end = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Title row with MAX amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontFamily = SpaceMono,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    color = primaryColor,
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .then(
                            if (maxClickable && showMaxAmount) Modifier.clickable {
                                val target = if (toggleFiat) formattedMaxFiatAmount else formattedMaxAmount
                                textFieldValue = TextFieldValue(
                                    text = target,
                                    selection = TextRange(target.length)
                                )
                                onAmountChange(target, toggleFiat)
                                onMaxClick()
                            } else Modifier
                        )
                )

                val maxAmount = if (toggleFiat) {
                    "$$formattedMaxFiatAmount"
                } else {
                    formattedMaxAmount
                }

                Text(
                    text = if (showMaxAmount) "MAX $maxAmount" else maxAmount,
                    fontFamily = PitagonsSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    color = primaryColor.copy(maxAlpha),
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .then(
                            if (maxClickable && showMaxAmount) Modifier.clickable {
                                val target = if (toggleFiat) formattedMaxFiatAmount else formattedMaxAmount
                                textFieldValue = TextFieldValue(
                                    text = target,
                                    selection = TextRange(target.length)
                                )
                                onAmountChange(target, toggleFiat)
                                onMaxClick()
                            } else Modifier
                        )
                )
            }

            // Amount input row with secondary content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAnyFieldFocused = remember { mutableStateOf(false) }
                
                DgenBasicTextfield(
                    modifier = Modifier.weight(1f),
                    value = textFieldValue,
                    onValueChange = { new ->
                        if (readOnly) {
                            textFieldValue = new
                        } else {
                            if (new.text == textFieldValue.text) {
                                textFieldValue = new
                            } else if (new.text.matches("^\\d*\\.?\\d*$".toRegex())) {
                                textFieldValue = new
                                onAmountChange(new.text, toggleFiat)
                            }
                        }
                    },
                    maxLines = 1,
                    maxLength = 42,
                    cursorColor = primaryColor,
                    placeholder = {
                        Text(
                            modifier = Modifier,
                            text = "0.0",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenWhite.copy(alpha = pulseOpacity),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 42.sp,
                                textAlign = TextAlign.Start,
                            )
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 42.sp,
                        textAlign = TextAlign.Start
                    ),
                    keyboardtype = KeyboardType.Number,
                    cursorWidth = 24.dp,
                    cursorHeight = 42.dp,
                    enabled = !readOnly,
                    readOnly = readOnly,
                    isAnyFieldFocused = isAnyFieldFocused
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                secondaryContent(secondarySelectable)
            }
        }
    }
}
