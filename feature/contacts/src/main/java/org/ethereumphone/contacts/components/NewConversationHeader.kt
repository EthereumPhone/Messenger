package org.ethereumphone.contacts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun NewConversationHeader(
    onBackClick: () -> Unit,
    title: String,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){

        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                contentDescription = "BackButton",
                modifier = Modifier.size(24.dp),
                tint = dgenTurqoise
            )
        }

        Text(
            text = title,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenTurqoise,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier,
        )

        IconButton(modifier = Modifier.alpha(0f), onClick = {  }) {
            Icon(
                painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                contentDescription = "BackButton",
                modifier = Modifier.size(24.dp),
                tint = dgenTurqoise
            )
        }

    }
}
