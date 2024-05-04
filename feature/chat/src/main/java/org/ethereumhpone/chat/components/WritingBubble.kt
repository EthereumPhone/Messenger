package org.ethereumhpone.chat.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.ethosmobile.components.library.theme.Colors
import kotlin.math.roundToInt

@Composable
fun WritingBubble(
    isUser: Boolean
){

    val inital = -5f
    val target = 5f
    val mode =  RepeatMode.Reverse
    val duration = 800
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val posY1 by infiniteTransition.animateFloat(
        initialValue = target,
        targetValue = inital,
        animationSpec = infiniteRepeatable(tween(duration,5), mode),
        label = "posY1"
    )
    val posY2 by infiniteTransition.animateFloat(
        initialValue = target,
        targetValue = inital,
        animationSpec = infiniteRepeatable(tween(duration,10), mode),
        label = "posY2"
    )
    val posY3 by infiniteTransition.animateFloat(
        initialValue = target,
        targetValue = inital,
        animationSpec = infiniteRepeatable(tween(duration,20), mode),
        label = "posY3"
    )

    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp),
        color = if(isUser) Color(0xFF8C7DF7) else Colors.DARK_GRAY,
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp,end = 16.dp,bottom = 16.dp,start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            BubbleCircle(
                circleSize = 8.dp,
                modifier = Modifier.graphicsLayer {
                    translationY = posY1
                }
            )
            BubbleCircle(
                circleSize = 8.dp,
                modifier = Modifier.graphicsLayer {
                    translationY = posY2
                }
            )
            BubbleCircle(
                circleSize = 8.dp,
                modifier = Modifier.graphicsLayer {
                    translationY = posY3
                }
            )
        }


    }
}
@Composable
@Preview
fun WritingBubblePreview(){

    WritingBubble(false)
}



@Composable
fun BubbleCircle(
    circleSize: Dp,
    modifier: Modifier = Modifier
){
    Canvas(modifier = modifier.size(circleSize)) {
        drawCircle(
            color = Colors.WHITE,
            center = this.center
        )
    }
}