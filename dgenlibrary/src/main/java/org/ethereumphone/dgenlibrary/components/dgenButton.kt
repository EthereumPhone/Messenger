package org.ethereumphone.dgenlibrary.components

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite

@Composable
fun dgenButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enable: Boolean = true,
){
    val buttonAlpha by animateFloatAsState(
        if (enable) 1f else 0.35f,
        tween(300)
    )

    Surface(
        color = dgenTurqoise,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                color = dgenOcean,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenWhite,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

    }
}


@Composable
fun dgenTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enable: Boolean = true,
){
    val buttonAlpha by animateFloatAsState(
        if (enable) 1f else 0.35f,
        tween(300)
    )

    Surface(
        color = dgenTurqoise,
        shape = CircleShape,
        modifier = Modifier
            .alpha(buttonAlpha)
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            }
    ){
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                color = dgenOcean,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenWhite,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

    }
}