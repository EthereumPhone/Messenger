package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethosmobile.components.library.theme.Colors

@Composable
@Preview
fun MessageReactions() {
    Box(modifier = Modifier


    ) {
        Row {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier

                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint= Colors.GRAY, imageVector = Icons.Filled.Favorite, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint= Colors.GRAY, imageVector = Icons.Filled.ThumbUp, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint= Colors.GRAY, imageVector = Icons.Filled.ThumbDown, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier.size(28.dp),tint= Colors.GRAY, imageVector = Icons.Filled.AddComment, contentDescription = "")
                }
                IconButton(modifier = Modifier.clip(CircleShape), onClick = { /*TODO*/ }) {
                    Icon(modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Colors.GRAY),tint= Colors.WHITE, imageVector = Icons.Rounded.Add, contentDescription = "")
                }

            }

        }
    }
}