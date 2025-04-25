package org.ethereumhpone.chat.components


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumhpone.chat.util.abbreviateNumber
import org.ethereumhpone.chat.util.formatSmart
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySendScreen(
    onBackClick: () -> Unit,
    onDone: () -> Unit,
    isGroup: Boolean = false
) {

    val scrollState = rememberLazyListState()
    var max by remember { mutableStateOf(0.0) }
    var amount by remember { mutableStateOf(TextFieldValue()) }
    var to by remember { mutableStateOf(TextFieldValue())}
    var token by remember { mutableStateOf("")}

    var readyToSend by remember { mutableStateOf(false)}
    
    var title = when(readyToSend){
        false ->  "SELECT TOKEN"
        true -> "SEND $${token.uppercase()}"
    }

    //TODO: Fill with a list of recipient
    val list = remember { mutableStateListOf(
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",
        "Max", "Joe", "Alex",

        ) }
    val preselectedRecipient = 0



    Column(Modifier.fillMaxSize().background(dgenBlack),
            verticalArrangement =  Arrangement.SpaceBetween) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {
                if(readyToSend){
                    readyToSend = false
                } else {
                    onBackClick()
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.backicon),
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
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )
            Spacer(Modifier.size(24.dp))

        }

        AnimatedContent(
            modifier = Modifier,
            targetState = readyToSend,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, 300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        ) { targetState ->
            if (!targetState){
                Box{
                    LazyColumn(
                        state= scrollState,
                        modifier = Modifier
                            .verticalLazyListScrollbar(scrollState) // Apply the scrollbar first
                            .fillMaxSize()
                            .background(dgenBlack)
                        ,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(Modifier.height(1.dp))
                        }

                        items(10) { index ->
                            //TODO: Add real tokens
                            AvailableToken(
                                name = "\$TOKEN $index",
                                balance = 1.2,
                                fiatamount=645.0,
                                modifier = Modifier.padding(horizontal = 32.dp).pointerInput(Unit){
                                    detectTapGestures {
                                        token = "TOKEN$index"// TODO: add token name
                                        readyToSend = true
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.fillMaxWidth().height(24.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(dgenBlack,Color.Transparent))))
                    Spacer(modifier = Modifier.fillMaxWidth().height(24.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent,dgenBlack))))
                }

            }
            else{
        Column (
            verticalArrangement =  Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 32.dp),
        ){
            Spacer(Modifier.height(8.dp))
             Column(
                 verticalArrangement = Arrangement.spacedBy(24.dp)
             ) {
                 Column(modifier = Modifier.fillMaxWidth()) {

                     Text(
                         text= "AMOUNT",
                         style = TextStyle(
                             fontFamily = SpaceMono,
                             color = dgenTurqoise,
                             fontWeight = FontWeight.Normal,
                             fontSize = 16.sp,
                             lineHeight = 16.sp,
                             letterSpacing = 0.sp,
                             textDecoration = TextDecoration.None
                         )
                     )
                     OldSchoolThickCursorTextField(
                         placeholder = {
                             Text(
                                 text = "0.0",
                                 style = TextStyle(
                                     fontFamily = PitagonsSans,
                                     color = dgenTurqoise.copy(alpha = 0.45f),
                                     fontWeight = FontWeight.SemiBold,
                                     fontSize = 56.sp
                                 )
                             )
                         },
                         value = amount,
                         onValueChange = { value ->
                             if (value.text.isNotEmpty() || value.text == "." || value.text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                                 // If it's a valid format or empty, call onAmountChange with the text
                                 amount = value
                             }

                         },
                         textStyle = TextStyle(
                             fontFamily = PitagonsSans,
                             color = dgenWhite,
                             fontWeight = FontWeight.Normal,
                             fontSize = 56.sp,
                             lineHeight = 56.sp,
                             letterSpacing = 0.sp,
                             textDecoration = TextDecoration.None
                         ),
                         cursorWidth = 16f,
                        cursorHeight = 100f,
                         hasMultipleLines = remember { mutableStateOf(false) },
                         cursorColor = dgenWhite,
                         modifier = Modifier.fillMaxWidth()
                     )
                     Text(
                         text = buildAnnotatedString {
                             append(abbreviateNumber(max))
                             withStyle(style = SpanStyle(
                                 fontFamily = PitagonsSans,
                                 color = dgenTurqoise,
                                 fontWeight = FontWeight.SemiBold,
                                 fontSize = 14.sp,
                                 letterSpacing = 0.sp,
                                 textDecoration = TextDecoration.None
                             )
                             ) {
                                 append(" $token available")
                             }

                         },
                         style = TextStyle(
                             fontFamily = PitagonsSans,
                             color = dgenTurqoise,
                             fontWeight = FontWeight.SemiBold,
                             fontSize = 16.sp,
                             lineHeight = 16.sp,
                             letterSpacing = 0.sp,
                             textDecoration = TextDecoration.None
                         )
                     )
                 }
                 Column(modifier = Modifier.fillMaxWidth()) {

                     Text(
                         text= "TO",
                         style = TextStyle(
                             fontFamily = SpaceMono,
                             color = dgenTurqoise,
                             fontWeight = FontWeight.Normal,
                             fontSize = 16.sp,
                             lineHeight = 16.sp,
                             letterSpacing = 0.sp,
                             textDecoration = TextDecoration.None
                         )
                     )
                     Box(modifier = Modifier.fillMaxWidth().animateContentSize().heightIn(max = 100.dp).padding(vertical = 12.dp)){
                         SelectableTextGrid(
                             list,
                             preselectedIndex = preselectedRecipient,
                             onSelectionChanged = {

                             }
                         )
                         Spacer(modifier = Modifier.fillMaxWidth().height(12.dp).align(Alignment.TopCenter).background(
                             Brush.verticalGradient(listOf(dgenBlack,Color.Transparent))))

                         Spacer(modifier = Modifier.fillMaxWidth().height(12.dp).align(Alignment.BottomCenter).background(
                             Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))

                     }

                 }
             }

             Row(
                 modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                 horizontalArrangement = Arrangement.Center
             ) {

                     Text(text= "SEND".uppercase(),
                         color = dgenTurqoise ,
                         style = TextStyle(
                             fontFamily = SpaceMono,
                             color = dgenTurqoise,
                             fontWeight = FontWeight.Bold,
                             fontSize = 24.sp,
                             lineHeight = 24.sp,
                             letterSpacing = 1.sp,
                             textDecoration = TextDecoration.None,
                             textAlign = TextAlign.Center
                         ),
                         modifier = Modifier.pointerInput(Unit){
                             detectTapGestures {
                                 //TODO: Send Transaction
                                 onDone()
                             }
                         }
                     )

             }

        }
            }
        }




    }


}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableTextGrid(
    items: List<String>,
    preselectedIndex: Int = -1,
    onSelectionChanged: (selectedIndex: Int) -> Unit = {}
) {
    // rememberSaveable if you want this to survive process death / config changes
    var selectedIndex by remember { mutableStateOf(preselectedIndex) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
    ) {
        itemsIndexed(items) { index, text ->

            RecipientName(
                selected = (index == selectedIndex),
                name = "$text $index",
                onClick = {
                    selectedIndex = index
                    onSelectionChanged(index)
                }

            )
        }
    }
}

@Composable
fun RecipientName(
    selected: Boolean,
    name: String,
    onClick: () -> Unit
){
    Text(
        modifier = Modifier.pointerInput(Unit){
            detectTapGestures {
                onClick()
            }
        },
        text = name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = SpaceMono,
            color = dgenTurqoise.copy(alpha = if (selected) 1f else 0.45f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    )
}

@Composable
fun AvailableToken(
    modifier: Modifier = Modifier,
    logoUrl: String = "",
    name: String = "",
    balance: Double = 0.0,
    fiatamount: Double = 0.0,
) {
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            when(logoUrl != ""){
                true ->{
                    AsyncImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(24.dp),
                        model = logoUrl,
                        contentDescription = "Translated description of what the image contains"
                    )
                }
                false -> {
                    Image(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(org.ethereumhpone.chat.R.drawable.unknown_token),
                        contentDescription = "Ethereum"
                    )

                }
            }
            Text(
                text = name,
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
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(

                buildAnnotatedString {
                    append(formatSmart(balance))

                    append("  ")

                    withStyle(style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise.copy(0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append("$" + decimalFormat.format(balance * fiatamount))
                    }

                },
                fontFamily = PitagonsSans,
                color = dgenTurqoise,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )



        }
    }
}



@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun OverlaySendScreenPreview(){
    OverlaySendScreen({},{})
}
