package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.pulseOpacity
import com.example.dgenlibrary.ui.theme.smallDuration

@Composable
fun TextToggle(
    modifier: Modifier = Modifier,
    textLeft: String,
    textRight: String,
    primaryColor: Color,
    onToggle: () -> Unit,
    value: Boolean // false = left, true = right
){
    val textStyle = SpanStyle(
        fontFamily = PitagonsSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp,
        textDecoration = TextDecoration.None
    )

    Crossfade(
        targetState = value,
        animationSpec = tween(smallDuration),
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { onToggle() }
        }
    ) { isRightSelected ->
        Text(
            buildAnnotatedString {
                withStyle(
                    style = textStyle.copy(
                        color = if (isRightSelected) primaryColor.copy(pulseOpacity) else primaryColor
                    )
                ) {
                    append(textLeft)
                }
                withStyle(
                    style = textStyle.copy(color = primaryColor)
                ) {
                    append(" / ")
                }
                withStyle(
                    style = textStyle.copy(
                        color = if (isRightSelected) primaryColor else primaryColor.copy(pulseOpacity)
                    )
                ) {
                    append(textRight)
                }
            }
        )
    }
}