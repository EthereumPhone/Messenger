package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.R

/**
 * Terminal-style action button component matching the WalletManager/TokenLauncher design.
 * A square button with an icon and text label, styled to match the terminal aesthetic.
 * 
 * This component is inspired by the ApeButton from TokenLauncher but adapted for 
 * the Messenger's transaction request functionality.
 * 
 * @param text The label text to display (e.g., "SEND", "REQUEST")
 * @param icon Icon resource ID to display
 * @param primaryColor Primary accent color
 * @param secondaryColor Secondary/background color
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the composable
 * @param enabled Whether the button is enabled/clickable
 * @param isActive Whether the button should display in active/highlighted state
 */
@Composable
fun TerminalActionButton(
    text: String,
    icon: Int,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    val buttonColor = if (isActive) primaryColor else Color.Transparent
    val textColor = if (isActive) secondaryColor else primaryColor
    val alpha = if (enabled) 1f else 0.4f

    Surface(
        modifier = modifier
            .widthIn(max = 88.dp)
            .aspectRatio(1f)
            .then(
                if (enabled) {
                    Modifier.clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else {
                    Modifier
                }
            ),
        color = buttonColor.copy(alpha = alpha),
        contentColor = primaryColor.copy(alpha = alpha),
        shape = RoundedCornerShape(3.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(icon),
                    contentDescription = text,
                    tint = textColor.copy(alpha = alpha)
                )
                Text(
                    text = text.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = textColor.copy(alpha = alpha),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

/**
 * Simplified terminal action button with just text (no icon).
 * Used for simpler action buttons that don't need an icon.
 */
@Composable
fun TerminalTextButton(
    text: String,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    val buttonColor = if (isActive) primaryColor else Color.Transparent
    val textColor = if (isActive) secondaryColor else primaryColor
    val alpha = if (enabled) 1f else 0.4f

    Surface(
        modifier = modifier
            .widthIn(max = 88.dp)
            .aspectRatio(1f)
            .then(
                if (enabled) {
                    Modifier.clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else {
                    Modifier
                }
            ),
        color = buttonColor.copy(alpha = alpha),
        contentColor = primaryColor.copy(alpha = alpha),
        shape = RoundedCornerShape(3.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = textColor.copy(alpha = alpha),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
