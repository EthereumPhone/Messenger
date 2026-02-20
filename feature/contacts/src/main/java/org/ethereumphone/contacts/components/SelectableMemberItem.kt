package org.ethereumphone.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun SelectableMemberItem(
    modifier: Modifier = Modifier,
    header: String,
    subheader: String = "",
    isSelected: Boolean,
    onCheckClick: () -> Unit = {},
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean
){
    // The entire row is clickable - clicking anywhere toggles selection
    Row(
        modifier
            .fillMaxWidth()
            .clickable { onCheckClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
        
        // Simple checkbox visual - not separately clickable (row click handles it)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isSelected) primaryColor else Color.Transparent,
                    shape = RoundedCornerShape(0.dp)
                )
                .then(
                    if (!isSelected) {
                        Modifier.drawBehind {
                            drawRect(
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = secondaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}