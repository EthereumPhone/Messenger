package org.ethereumhpone.contracts.ui

import android.util.Log
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


enum class ChatListItemState{
    CLOSED,
    OPENRIGHT,
    OPENLEFT
}

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun ChatListItem(
//){
//    Box{
//
//        val unreadWidth = -72.dp
//        val optionWidth = 128.dp
//
//        val anchors = DraggableAnchors {
//            ChatListItemState.OPENLEFT at unreadWidth.value
//            ChatListItemState.CLOSED at 0f
//            ChatListItemState.OPENRIGHT at optionWidth.value
//
//        }
//
//
//
//        val density = LocalDensity.current
//        val state = remember {
//            AnchoredDraggableState (
//                initialValue = ChatListItemState.CLOSED,
//                anchors = anchors,
//                positionalThreshold = { distance: Float -> distance * 0.5f },
//                animationSpec = spring(),
//                velocityThreshold = { with(density) { 186.dp. toPx() } },
//            )
//        }
//
//
//
//
//        ChatListUnreadSection({})
//        ChatListOptions({})
//        Row (
//            modifier = Modifier
//                .fillMaxWidth()
//                .graphicsLayer {
//                    this.translationX = state.requireOffset()
//                }
//                .anchoredDraggable(state, Orientation.Horizontal)
//        ){
//            ChatListInfo(
//                header = "Mark Katakowskihashvili",
//                subheader = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd ",
//            )
//        }
//
//
//    }
//
//}

@Preview
@Composable
fun PreviewChatListUnreadSection(){
    ChatListUnreadSection({})
}
@Composable
fun ChatListUnreadSection(
    onClick: () -> Unit = {}
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(86.dp)
            .background(Colors.LIGHT_BLUE)
        ,
    ) {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxHeight()
                .width(72.dp)
                .background(Colors.LIGHT_BLUE)
        ){
            Icon(
                imageVector = Icons.Filled.MarkChatUnread,
                contentDescription = "Overlay Icon",
                tint = Colors.WHITE,
            )
        }



    }
}

@Preview
@Composable
fun PreviewChatListOptions(){
    ChatListOptions({})
}
@Composable
fun ChatListOptions(
    onClick: () -> Unit = {}
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(Colors.DARK_GRAY)

        ,
    ) {

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxHeight()
                .width(72.dp)
                .background(Colors.LIGHT_BLUE)
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                contentDescription = "VolumeMute",
                tint = Colors.WHITE,
            )
        }

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxHeight()
                .width(72.dp)
                .background(Colors.ERROR)
        ){
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = "DeleteForever",
                tint = Colors.WHITE,
            )
        }


    }
}


@Composable
fun ChatListInfo(
    image: @Composable () -> Unit = {},
    header: String = "Header",
    subheader: String = "Subheader",
    time: Date?,//"0:00AM",
    unreadConversation: Boolean = true,
    onClick: () -> Unit = {}, //threadId long -> String
    modifier: Modifier = Modifier
) {

    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ,
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(CircleShape)
                .background(Colors.DARK_GRAY)
                .size(62.dp)

        ) {
            image()
        }
        Column (
            modifier = modifier.weight(1f)
        ){
            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.fillMaxWidth()//.background(Color.Blue)
            ){
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier.weight(1f)
                ){
                    Text(
                        text = header,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.WHITE,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = modifier.widthIn(min = 80.dp, max = 180.dp)

                    )
                }



                Column (
                    horizontalAlignment = Alignment.End,
                    modifier = modifier
                        .weight(0.35f)
                ){
                    printFormattedDateInfo(time)?.let {
                        Text(
                            text = it,
                            color = Colors.GRAY,
                            style = TextStyle(
                                color = Colors.GRAY,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Fonts.INTER,
                            ),
                        )
                    }
                }
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
                    modifier = modifier.weight(1f)
                )
                if (unreadConversation){
                    Box (
                        contentAlignment = Alignment.CenterEnd,
                        modifier = modifier
                            .weight(0.35f)
                    ){
                        Box (
                            modifier = modifier
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


fun printFormattedDateInfo(date: Date?): String? {


    val formattedDate = date?.let { formatDate(it) }

    val calendar = Calendar.getInstance()
    if (date != null) {
        calendar.time = date
    }

    val currentCalendar = Calendar.getInstance()

    when {
        date?.let { isWithinLast7Days(it) } == true -> {
            val weekday = getWeekday(date)
            if (isSameDay(calendar, currentCalendar)){
                return formattedDate
            }

            return weekday
        }
        date?.let { isBeforeLast7Days(it) } == true -> {
            println("The date $formattedDate is before the last 7 days.")
            return formattedDate
        }
        else -> {
            println("The date $formattedDate is not within the last 7 days and not before the last 7 days (i.e., it's in the future).")
            return formattedDate
        }
    }
}

fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val currentCalendar = Calendar.getInstance()

    return when {
        isSameDay(calendar, currentCalendar) -> {
            SimpleDateFormat("HH:mm").format(date)
        }
        calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) -> {
            SimpleDateFormat("MM.dd").format(date)
        }
        else -> {
            SimpleDateFormat("yyyy.MM.dd").format(date)
        }
    }
}

fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

fun isWithinLast7Days(date: Date): Boolean {
    val currentDate = Date()
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = currentDate
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return !date.before(sevenDaysAgo) && !date.after(currentDate)
}

fun isBeforeLast7Days(date: Date): Boolean {
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = Date()
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return date.before(sevenDaysAgo)
}

fun getWeekday(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
    }
}

@Preview
@Composable
fun PreviewContactItem(){
//    ChatListInfo(
//        header = "Mark Katakowskihashvili",
//        subheader = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd ",
//    )
}