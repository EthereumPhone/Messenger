package org.ethereumphone.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun MemberItem(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    header: String,
    subheader: String = "",
    primaryColor: Color = dgenTurqoise
){
    var openDelete by remember { mutableStateOf(false) }
    Row (
        modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        openDelete = !openDelete
                    },
                    onDoubleTap = {
                        openDelete = !openDelete
                    }
                )
            }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ){
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = header,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
            if(subheader.isNotEmpty()){
                Text(
                    text = subheader,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor.copy(0.45f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        AnimatedVisibility(
            openDelete,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                Icons.Outlined.Delete,
                "Delete",
                tint = dgenRed,
                modifier = Modifier
                    .size(28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onDelete()
                        }
                    }
            )
        }
    }
}