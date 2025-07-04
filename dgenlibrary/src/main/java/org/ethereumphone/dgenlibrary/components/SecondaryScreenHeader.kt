package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.body1_fontSize
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.theme.DgenTheme

@Composable
fun SecondaryScreenHeader(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    title: String,
    onDismiss: () -> Unit,
){
    Row(
        modifier = modifier.padding(end = 12.dp, start = 24.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = body1_fontSize,
                lineHeight = body1_fontSize,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.baseline_close_24),
                contentDescription = "Back Button",
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}