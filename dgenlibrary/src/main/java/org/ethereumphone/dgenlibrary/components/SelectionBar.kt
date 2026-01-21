package org.ethereumphone.dgenlibrary.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.smalllabel_fontSize
import org.ethereumphone.dgenlibrary.theme.DgenTheme
import org.ethereumphone.dgenlibrary.theme.dgenBlack

@Composable
fun SelectionBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    headerText: String = "",
    primaryColor: Color,
    onCancelClick: () -> Unit,
    showHeader: Boolean = true,
    showCancelButton: Boolean = true,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colorStops = arrayOf(
        0.0f to Color.Transparent,
        0.1f to dgenBlack,
    )

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .wrapContentHeight()
                .background(brush = Brush.verticalGradient(colorStops = colorStops))
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (headerContent != null) {
                        headerContent()
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Text(
                            text = headerText.uppercase(),
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = DgenTheme.typography.label.fontSize,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None,
                                textAlign = TextAlign.Start
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (showCancelButton) {
                        IconButton(
                            onClick = onCancelClick,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Image(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.Outlined.Close,
                                colorFilter = ColorFilter.tint(primaryColor),
                                contentDescription = "Close selection"
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Transparent)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Extra actions provided by the caller
                actions()
            }
        }
    }
}


@Composable
fun RowScope.SelectionBarColumn(
    imageVector: ImageVector,
    title: String,
    primaryColor: Color,
    onItemLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val tintColor = primaryColor
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(minHeight = 80.dp)
            .weight(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onItemLongClick?.invoke() }
                )
            }
//            .debouncedCombinedClickable(
//                onClick = onClick,
//                onLongClick = onItemLongClick
//            )
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = imageVector,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = title,
            modifier = Modifier
                .height(40.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title.uppercase(),
            modifier = Modifier,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = smalllabel_fontSize,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None,
                textAlign = TextAlign.Center
            ),
            color = tintColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RowScope.SelectionBarColumn(
    icon: Int,
    title: String,
    primaryColor: Color,
    onItemLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val tintColor = primaryColor
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(minHeight = 80.dp)
            .weight(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onItemLongClick?.invoke() }
                )
            }
//            .debouncedCombinedClickable(
//                onClick = onClick,
//                onLongClick = onItemLongClick
//            )
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(icon),
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = title,
            modifier = Modifier
                .height(40.dp).scale(1.2f)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title.uppercase(),
            modifier = Modifier,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = smalllabel_fontSize,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None,
                textAlign = TextAlign.Center
            ),
            color = tintColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectionBar_Default() {
    SelectionBar(
        visible = true,
        primaryColor = Color.White,
        onCancelClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectionBar_with_Composable() {
    SelectionBar(
        visible = true,
        primaryColor = Color.White,
        headerContent = {
            Text(
                text = "header Content".uppercase(),
                modifier = Modifier,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = smalllabel_fontSize,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Center
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text("headerContent")
        },
        onCancelClick = {},
    ){
        SelectionBarColumn(
            imageVector = Icons.Outlined.Visibility,
            title = "Hide",
            primaryColor = Color.White,
            onClick = {}
        )
        SelectionBarColumn(
            imageVector = Icons.Outlined.VisibilityOff,
            title = "Unhide",
            primaryColor = Color.White,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectionBar_WithExtraActions() {
    SelectionBar(
        visible = true,
        headerText = "Ethereum",
        primaryColor = Color.White,
        onCancelClick = {},
    ) {
        SelectionBarColumn(
            imageVector = Icons.Outlined.Visibility,
            title = "Hide",
            primaryColor = Color.White,
            onClick = {}
        )
        SelectionBarColumn(
            imageVector = Icons.Outlined.VisibilityOff,
            title = "Unhide",
            primaryColor = Color.White,
            onClick = {}
        )
    }
}