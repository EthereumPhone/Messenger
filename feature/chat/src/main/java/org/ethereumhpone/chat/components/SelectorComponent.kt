package org.ethereumhpone.chat.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumhpone.chat.R
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun SelectorComponent(
    currentInputSelector: MutableState<InputSelector>,
    onSelectorChange: (InputSelector) -> Unit,
    onShowSelectionbar: () -> Unit,
    onHideKeyboard: () -> Unit,
){
    Row (
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)

    ){
        ActionButton(
            onSelectorChange = { onSelectorChange(InputSelector.EMOJI) },
            onHideKeyboard = onHideKeyboard,
            onShowSelectionbar = onShowSelectionbar,
            isActive = currentInputSelector.value == InputSelector.EMOJI,
            title = "Emoji",
            icon = Icons.Outlined.Mood
        )

        Spacer(modifier = Modifier.width(12.dp))
        ActionButton(
            onSelectorChange = { onSelectorChange(InputSelector.WALLET) },
            onHideKeyboard = onHideKeyboard,
            onShowSelectionbar = onShowSelectionbar,
            isActive = currentInputSelector.value == InputSelector.WALLET,
            title = "Transfer",
            icon = ImageVector.vectorResource(R.drawable.wallet)
        )

        Spacer(modifier = Modifier.width(12.dp))

        ActionButton(
            onSelectorChange = { onSelectorChange(InputSelector.PICTURE) },
            onHideKeyboard = onHideKeyboard,
            onShowSelectionbar = onShowSelectionbar,
            isActive = currentInputSelector.value == InputSelector.PICTURE,
            title = "Send",
            icon = Icons.Outlined.InsertPhoto
        )
    }
}


@Composable
fun ActionButton(
    onSelectorChange: () -> Unit,
    onShowSelectionbar: () -> Unit,
    onHideKeyboard: () -> Unit,
    isActive: Boolean,
    title: String,
    icon: ImageVector
){



    val bgColor by animateColorAsState(
        if (isActive) Colors.WHITE else Colors.DARK_GRAY,
        label = "color"
    )

    val iconColor by animateColorAsState(
        if (isActive) Colors.DARK_GRAY else Colors.WHITE,
        label = "iconcolor"
    )



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)

            ,
            colors =  IconButtonDefaults.iconButtonColors(
                containerColor = bgColor,
                // contentColor = if (isActive) Colors.DARK_GRAY else Colors.WHITE,
            ),
            onClick = {
                    onSelectorChange()
                    onShowSelectionbar()
                    onHideKeyboard()
            },
        ) {

                Icon(
                    imageVector = icon,
                    modifier= Modifier.size(32.dp),
                    contentDescription = "Send",
                    tint = iconColor
                )


        }

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight =  FontWeight.Medium,
                    color = Colors.WHITE,
                    fontFamily = Fonts.INTER
                )
            )


    }
}