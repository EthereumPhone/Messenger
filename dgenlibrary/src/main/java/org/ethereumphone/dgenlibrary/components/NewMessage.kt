package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
fun NewMessage(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

        Surface(
            modifier = modifier.pointerInput(Unit){
                detectTapGestures{
                    onClick()
                }
            },
            shape = RoundedCornerShape(12.dp),
            color = dgenTurqoise
        ) {
            Text(
                text = "New Message".uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenOcean,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }

}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun NewMessagePreview(){
    NewMessage()
}