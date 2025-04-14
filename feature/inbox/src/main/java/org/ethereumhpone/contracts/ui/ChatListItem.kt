package org.ethereumhpone.contracts.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.datetime.Instant
import org.ethereumhpone.contracts.utils.printFormattedDateInfo
import java.util.Date
import java.time.Instant as JavaInstant



@Composable
fun ChatListInfo(
    header: String = "Header",
    subheader: String = "Subheader",
    lastPerson: String = "",
    unReadMessagesAmount: Int = 1,
    time: Date,//"0:00AM",
    isGroup: Boolean = false,
    readConversation: Boolean = false,
    onClick: () -> Unit = {}, //threadId long -> String
    modifier: Modifier = Modifier
) {

    Row (
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 16.dp, horizontal = 24.dp)
        ,
    ){
        Column(
            modifier = modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = header,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = if(readConversation) dgenTurqoise.copy(0.5f) else dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = modifier.widthIn(min = 80.dp, max = 180.dp)

            )
            Text(
                text = if(isGroup) "$lastPerson: $subheader" else subheader,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = if(readConversation) dgenTurqoise.copy(0.5f) else dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = modifier
            )
        }

        Column (
            horizontalAlignment = Alignment.End,
            modifier = modifier
                .weight(0.25f)
        ){
            printFormattedDateInfo(time)?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = if(readConversation) dgenTurqoise.copy(0.5f) else dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                )
            }

            /*
                    if(!readConversation && isGroup)  {
                        Surface(
                            shape = CircleShape,
                            color = dgenTurqoise,
                        ) {
                            Text(
                                modifier = modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                text = "TX REQUEST",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenOcean,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 8.sp,
                                    lineHeight = 8.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                            )
                        }
                    }
             */


        }

    }

}




@Preview
@Composable
fun PreviewContactItem(){
    val context = LocalContext.current
    val now = Instant.parse("2025-04-10T10:00:00Z")

    ChatListInfo(
        onClick = { Toast.makeText(context, "onClick", Toast.LENGTH_SHORT).show() },
        header = "Alex Lynn",
        subheader = "The dGEN1 is sick!!!",
        time = Date.from(JavaInstant.parse(now.toString())),
        isGroup = false,
    )
}