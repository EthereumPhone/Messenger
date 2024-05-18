package org.ethereumhpone.chat.components

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumhpone.database.model.Message
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun MessageActionList(
    message: Message,
    focusMode: MutableState<Boolean>,
) {
    val context = LocalContext.current

    Box(modifier = Modifier
        .graphicsLayer {
            shape = RoundedCornerShape(12.dp)
            clip = true
        }
        .background(Colors.DARK_GRAY)
        .width(200.dp)

    ) {
        Column {
            MessageAction(
                text = "Copy",
                imageVector = Icons.Outlined.ContentCopy,
                onClick = {
                    copyTextToClipboard(context,message.body)
                    focusMode.value = false
                }
            )
            Divider(color= Colors.GRAY)
            MessageAction(
                text = "Info",
                imageVector = Icons.Outlined.Info,
                onClick = {
                    Toast.makeText(context,"Info",Toast.LENGTH_SHORT).show()
                }
            )
            Divider(color= Colors.GRAY)
            MessageAction(
                text = "Delete",
                tint= Colors.ERROR,
                imageVector = Icons.Outlined.Delete,
                onClick = {
                    Toast.makeText(context,"Delete",Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}


@Composable
fun MessageAction(
    text: String,
    tint: Color = Colors.WHITE,
    imageVector: ImageVector,
    onClick: () -> Unit
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable{
                onClick()
            }
    ) {
        Text(text = text, fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, color= tint)
        Icon(tint = tint, modifier = Modifier.size(20.dp), imageVector = imageVector, contentDescription = text)
    }
}


@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}
