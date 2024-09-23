package org.ethereumhpone.contracts.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.coroutineContext


enum class ChatListItemState{
    CLOSED,
    OPENRIGHT,
    OPENLEFT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    image: @Composable () -> Unit = {},
    header: String = "Header",
    subheader: String = "Subheader",
    time: Date? = Date(),
    unreadConversation: Boolean = true,
    onClick: () -> Unit = {},
    onClickLeft: () -> Unit = {},
    onClickRight: () -> Unit = {},
    isInbox: Boolean = true,
    modifier: Modifier = Modifier
){

    val leftWidth = (-120*2).dp //-400.dp
    val rightWidth = (120*2).dp

    val anchors = DraggableAnchors {
        ChatListItemState.OPENLEFT at leftWidth.value
        ChatListItemState.CLOSED at 0.dp.value
        ChatListItemState.OPENRIGHT at rightWidth.value

    }



    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        AnchoredDraggableState (
            initialValue = ChatListItemState.CLOSED,
            anchors = anchors,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            animationSpec = spring(),
            velocityThreshold = { with(density) { 50.dp.toPx() } },
        )
    }
    Box{
        ChatListOptions(
            isInbox = isInbox,
            onClickLeft = {
                onClickLeft()
                coroutineScope.launch {
                    state.animateTo(ChatListItemState.CLOSED)
                }
            },
            onClickRight = {
                onClickRight()
                coroutineScope.launch {
                    state.animateTo(ChatListItemState.CLOSED)
                }
            }
        )
        Row (
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.translationX = state.requireOffset()
                }
                .anchoredDraggable(state, Orientation.Horizontal)
        ){
            ChatListInfo(
                image = image,
                header = header,
                subheader = subheader,
                time = time,
                unreadConversation = unreadConversation,
                modifier = modifier,
                onClick = onClick
            )
        }
    }
}


@Preview
@Composable
fun PreviewChatListOptions(){
    ChatListOptions()
}
@Composable
fun ChatListOptions(
    isInbox: Boolean = true,
    onClickLeft: () -> Unit = {},
    onClickRight: () -> Unit = {},
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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable { onClickLeft() }
                .fillMaxHeight()
                .weight(0.4f)
                .background(Colors.LIGHT_BLUE)
        ){
            Icon(
                imageVector = if(isInbox) Icons.Filled.Archive else Icons.Filled.Unarchive,
                contentDescription = "Archive",
                tint = Colors.WHITE,
            )
        }


        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier

                .fillMaxHeight()
                .weight(1f)
                .background(Colors.BLACK)
                .padding(start = 28.dp)
        ){

        }


        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable { onClickRight() }
                .fillMaxHeight()
                .weight(0.4f)
                .background(Colors.ERROR)
        ){
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = "Delete",
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
            .background(Colors.BLACK)
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
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
            return formattedDate
        }
        else -> {
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
    val context = LocalContext.current
    ChatListItem(
        onClick = { Toast.makeText(context, "onClick", Toast. LENGTH_SHORT). show() },
        onClickLeft = { Toast.makeText(context, "Left", Toast. LENGTH_SHORT). show() },
        onClickRight = { Toast.makeText(context, "Right", Toast. LENGTH_SHORT). show() }
    )
}