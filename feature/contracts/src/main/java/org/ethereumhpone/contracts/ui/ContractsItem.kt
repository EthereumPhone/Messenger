package org.ethereumhpone.contracts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.ethosmobile.components.library.core.ethOSChatListItem
import org.ethosmobile.components.library.core.ethOSTag
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun ChatListItem(
    image: @Composable () -> Unit = {},
    header: String = "Header",
    subheader: String = "Subheader",
    ens: String = "",
    time: String = "0:00AM",
    unreadConversation: Boolean = true,
    onClick: () -> Unit = {},
) {

    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ,
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .background(Colors.DARK_GRAY)
                .size(62.dp)

        ) {
            image()
        }
        Column (
            modifier = Modifier.weight(1f)
        ){
            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()//.background(Color.Blue)
            ){
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = header,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.WHITE,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.widthIn(min = 80.dp, max = 180.dp)

                    )
//                    Text(
//                        text = ens,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Normal,
//                        color = Colors.GRAY,
//                        overflow = TextOverflow.Ellipsis,
//                        maxLines = 1,
//                        modifier = Modifier.widthIn(min = 80.dp, max = 180.dp)
//
//                    )
                }

                Text(
                    text = time,
                    color = Colors.GRAY,
                    style = TextStyle(
                        color = Colors.GRAY,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Fonts.INTER,
                    ),
                    modifier = Modifier.weight(0.25f)
                )
            }

            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(
                    text = subheader,
                    style = TextStyle(
                        color = if(unreadConversation) Colors.WHITE else Colors.GRAY,
                        fontSize = 14.sp,
                        fontWeight = if(unreadConversation) FontWeight.SemiBold else FontWeight.Normal,
                        fontFamily = Fonts.INTER,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                if (unreadConversation){
                    Box (
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .weight(0.25f)
                    ){
                        Box (
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF8C7DF7))
                                .size(14.dp)

                        ){

                        }

                    }
                }
            }

        }

    }

}

@Preview
@Composable
fun PreviewContactItem(){
    ChatListItem(
        header = "Mark Katakowskihashvili",
        subheader = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd ",
        ens = "emunsi.eth",
    )
}