package org.ethereumhpone.chat.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun ChatBottomAppBar(
    enableSending: Boolean,
    onSendClick: (String) -> Unit
) {



    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    var lastFocusState by remember { mutableStateOf(false) }

    BottomAppBar(
        containerColor = Color.Black
    ) {
        Row {
            TextField(
                shape = RoundedCornerShape(35.dp),
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(35.dp))
                    .border(
                        2.dp,
                        Colors.DARK_GRAY,
                        RoundedCornerShape(35.dp)
                    )
                    .heightIn(min = 56.dp, max = 100.dp)
                    ,
                placeholder = { Text("Type a message") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Colors.WHITE,
                    unfocusedTextColor = Colors.WHITE,
                    focusedContainerColor = Colors.TRANSPARENT,
                    unfocusedContainerColor = Colors.TRANSPARENT,
                    disabledContainerColor = Colors.TRANSPARENT,
                    cursorColor = Colors.WHITE,
                    errorCursorColor = Colors.WHITE,
                    focusedBorderColor = Colors.TRANSPARENT,
                    unfocusedBorderColor = Colors.TRANSPARENT,
                    focusedPlaceholderColor = Colors.GRAY,
                    unfocusedPlaceholderColor = Colors.GRAY,
                ),
                textStyle = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontFamily = Fonts.INTER,
                    fontSize = 18.sp,
                    color = Colors.WHITE,
                ),
            )
        }
    }
}