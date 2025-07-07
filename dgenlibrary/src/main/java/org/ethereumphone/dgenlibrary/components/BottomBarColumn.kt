package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.smalllabel_fontSize
import org.ethereumphone.dgenlibrary.theme.DgenTheme

//@Composable
//fun <T : Media> BottomBarColumn(
//    currentMedia: T?,
//    imageVector: ImageVector,
//    title: String,
//    enabled: Boolean = true,
//    followTheme: Boolean = false,
//    onItemLongClick: ((T) -> Unit)? = null,
//    primaryColor: Color,
//    onItemClick: (T) -> Unit,
//) {
//    val alpha = if (enabled) 1f else 0.5f
//    val tintColor =
//        if (followTheme) primaryColor.copy(alpha = alpha) else primaryColor.copy(
//            alpha = alpha
//        )
//    Column(
//        modifier = Modifier
//            .clip(RoundedCornerShape(12.dp))
//            .defaultMinSize(
//                minWidth = 90.dp
//            )
//            .height(84.dp)
//            .combinedClickable(
//                enabled = enabled,
//                onLongClick = {
//                    currentMedia?.let {
//                        onItemLongClick?.invoke(it)
//                    }
//                },
//                onClick = {
//                    currentMedia?.let {
//                        onItemClick.invoke(it)
//                    }
//                }
//            )
//            .padding(top = 12.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            imageVector = imageVector,
//            contentDescription = title,
//            colorFilter = ColorFilter.tint(color = tintColor),
//            modifier = Modifier
//                .height(24.dp)
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = title.uppercase(),
//            style = DgenTheme.typography.label,
//            fontSize = smalllabel_fontSize,
//            color = tintColor,
//            textAlign = TextAlign.Center,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}