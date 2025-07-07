package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite

@Composable
fun dgenButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enable: Boolean = true,
    backgroundColor: Color,
    fontColor: Color,
    fontSize: TextUnit = 20.sp,
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 1.dp,
){
    val buttonAlpha by animateFloatAsState(
        if (enable) 1f else 0.35f,
        tween(300)
    )

    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = modifier
            .alpha(buttonAlpha)
            .animateContentSize()
            .pointerInput(Unit) {
                 detectTapGestures {
                    onClick()
                }
            }
    ){
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                color = fontColor,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
            )
        }

    }
}

@Preview
@Composable
fun DgenButtonPreview(){
    dgenButton(onClick = {}, text = "GRANT PERMISSION", backgroundColor = dgenTurqoise, fontColor = dgenOcean)
}

@Composable
fun dgenTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enable: Boolean = true,
    fontColor: Color,
    fontSize: TextUnit = 20.sp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 1.dp,
    primaryColor: Color
){
    val buttonAlpha by animateFloatAsState(
        if (enable) 1f else 0.35f,
        tween(300)
    )

    Surface(
        color = Color.Transparent,
        shape = CircleShape,
        modifier = modifier
            .alpha(buttonAlpha)
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            }
    ){
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                color = fontColor,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
            )
        }

    }
}