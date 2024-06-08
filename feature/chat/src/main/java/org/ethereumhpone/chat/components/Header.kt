package org.ethereumhpone.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.R
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun Header(
    modifier: Modifier = Modifier,
    name: String,
    image: String,
    onPhoneClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
){
    val iconsize = 24.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = modifier.size(iconsize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint =  Colors.WHITE,
                        modifier = modifier.size(iconsize)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ){
                        if (image != ""){
//                    Image(painter = painterResource(id = R.drawable.nouns), contentDescription = "" )
                            Image(
                                painter = rememberAsyncImagePainter(image),
                                contentDescription = "Contact Profile Pic",
                                contentScale = ContentScale.Crop
                            )
                        } else{
                            Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                        }
                    }

                    Text(
                        textAlign = TextAlign.Center,
                        text = name,
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Fonts.INTER,
                    )

                }
            }



//      Warning or info
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                ) {
                    IconButton(
                        onClick = onPhoneClick,
                        modifier = modifier.size(iconsize)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = "Go back",
                            tint =  Colors.WHITE,
                            modifier = modifier.size(iconsize)
                        )
                    }
                }
            }
        }

    }

}

@Composable
fun ChatHeader(
    modifier: Modifier = Modifier,
    name: String,
    ens: List<String>,
    image: String,
    onBackClick: () -> Unit = {},
    onPhoneClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
){
    val iconsize = 24.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = modifier.size(iconsize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint =  Colors.WHITE,
                        modifier = modifier.size(iconsize)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ){
                        if (image != ""){
                            Image(
                                painter = rememberAsyncImagePainter(image),
                                contentDescription = "Contact Profile Pic",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else{
                            Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                        }
                    }

                    Column (
                        modifier = modifier.clickable {
                            onContactClick()
                        }
                    ){
                        Text(
                            textAlign = TextAlign.Center,
                            text = name,
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Fonts.INTER,
                        )

                        if (ens.isNotEmpty()){
                            val enss = getEnsAddresses(ens)
                            Text(
                                textAlign = TextAlign.Center,
                                text = enss,
                                fontSize = 14.sp,
                                color = Colors.GRAY,
                                fontWeight = FontWeight.Normal,
                                fontFamily = Fonts.INTER,
                            )
                        }

                    }


                }
            }



//      Warning or info
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                ) {
                    IconButton(
                        onClick = onPhoneClick,
                        modifier = modifier.size(iconsize)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = "Go back",
                            tint =  Colors.WHITE,
                            modifier = modifier.size(iconsize)
                        )
                    }
                }
            }
        }

    }

}

fun getEnsAddresses(ens: List<String>): String{
    var res = ""

    ens.forEachIndexed{ index, value ->
        res += value
        if (index < ens.size-1){
            res += ", "
        }
    }
    return res
}

@Composable
@Preview
fun PreviewHeader() {
    Header(
        name = "Mark Katakowski",
        image = "",
        onBackClick = {},
    )
}