package org.ethereumhpone.contracts.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.database.model.Contact
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSheet(
    contacts: List<Contact> = emptyList(),
    onPhoneNumberSelected: (String) -> Unit = {},
    onSelectContact: (Contact) -> Unit = {},
) {

    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }
    var textFieldFocusState by remember { mutableStateOf(false) }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var phoneNumber by remember { mutableStateOf(TextFieldValue()) }
    var showPhoneNumberDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .padding(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Contacts",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Colors.BLACK)
            ) {
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(42.dp)
                    ,
                    enabled = true,
                    onClick = {
                        showPhoneNumberDialog = true
                    },
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ){
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add contact", tint = Colors.WHITE, modifier = Modifier.size(32.dp))
                    }

                }
            }

            if (showPhoneNumberDialog) {
                Dialog(
                    onDismissRequest = { showPhoneNumberDialog = false },
                    properties = DialogProperties(dismissOnClickOutside = false)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = Color.Black,  // Set dialog background to black
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Enter Phone Number",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White  // Change text color to white
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneNumber = it
                                },
                                label = { Text("Phone Number", color = Color.White) },  // Change label text color to white
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,  // Set text color to white when focused
                                    unfocusedTextColor = Color.White,  // Set text color to white when unfocused
                                    disabledTextColor = Color.Gray,  // Set text color to gray when disabled
                                    errorTextColor = Color.Red,  // Set text color to red when there's an error
                                    focusedContainerColor = Color.DarkGray,  // Set background color to dark gray when focused
                                    unfocusedContainerColor = Color.DarkGray,  // Set background color to dark gray when unfocused
                                    disabledContainerColor = Color.LightGray,  // Set background color to light gray when disabled
                                    errorContainerColor = Color.Red,  // Set background color to red when there's an error
                                    cursorColor = Color.White,  // Set cursor color to white
                                    errorCursorColor = Color.Red,  // Set cursor color to red when there's an error
                                    focusedIndicatorColor = Color.White,  // Set focused indicator color to white
                                    unfocusedIndicatorColor = Color.White  // Set unfocused indicator color to white
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { showPhoneNumberDialog = false }) {
                                    Text("Cancel", color = Color.White)  // Change button text color to white
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        // Handle phone number input
                                        showPhoneNumberDialog = false
                                        onPhoneNumberSelected(phoneNumber.text)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.DarkGray,  // Change button background color
                                        contentColor = Color.White  // Change button content color to white
                                    )
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

            }
        }


        if (contacts.isEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No contacts available",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Fonts.INTER,
                    color = Colors.GRAY,
                )
            }
        }else{
            Spacer(modifier = Modifier.height(24.dp))
            SearchTextField(
                textFieldValue = textState,
                onTextChanged = { textState = it},
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
//                    resetScroll()
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState,

                )

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn{
                    contacts.filter { it.name.contains(textState.text) }.forEach {
                        item {
                            ethOSContactListItem(
                                withImage = it.photoUri != null,
                                image = {
                                    Image(
                                        painter = rememberAsyncImagePainter(it.photoUri),
                                        contentDescription = "Contact profile pic",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                },
                                header = it.name,
                                withSubheader = false,// ens in future ?
                                onClick = {
                                    onSelectContact(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }





}

@Composable
private fun SearchTextField(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusState: Boolean,
    modifier: Modifier = Modifier,

    ) {




    var lastFocusState by remember { mutableStateOf(false) }
    Row (
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, Colors.GRAY, CircleShape)
    ){
        BasicTextField(
            value = textFieldValue,
            onValueChange = { onTextChanged(it) },
            modifier = modifier
                .padding(12.dp)
                .onFocusChanged { state ->
                    if (lastFocusState != state.isFocused) {
                        onTextFieldFocused(state.isFocused)
                    }
                    lastFocusState = state.isFocused
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Send
            ),

            cursorBrush = SolidColor(
                Colors.WHITE
            ),
            textStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontFamily = Fonts.INTER,
                fontSize = 18.sp,
                color = Colors.WHITE,
            )
        )
        { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Searching",
                    tint = Colors.GRAY,
                    modifier = Modifier.size(28.dp)
                )
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier

                        ,
                        text = "Search contact",
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Fonts.INTER,
                        fontSize = 18.sp,
                        color = Colors.GRAY,
                    )
                }else{

                    // Send button
                    innerTextField()
                }



            }
        }
    }



}

@Composable
fun ethOSContactListItem(
    withImage:Boolean = false,
    image: @Composable () -> Unit = {},
    header: String = "Header",
    withSubheader: Boolean = false,
    subheader: String = "Subheader",
    trailingContent: @Composable (() -> Unit)? = null,
    backgroundColor: Color = Colors.TRANSPARENT,
    colorOnBackground: Color = Colors.WHITE,
    subheaderColorOnBackground: Color = Colors.WHITE,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier

) {


    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() },
    ) {
        if(withImage){
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .size(56.dp)
            ) {
                image()
            }
        }

        ListItem(
            headlineContent = {
                Text(
                    text = header,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Colors.WHITE,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            supportingContent = {
                if(withSubheader){
                    Text(
                        text = subheader,
                        color = Colors.GRAY,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            },
            trailingContent = trailingContent,

            colors = ListItemDefaults.colors(
                headlineColor = colorOnBackground,
                supportingColor = subheaderColorOnBackground,
                containerColor = backgroundColor
            )
        )

    }

}


@Composable
@Preview
fun ContactSheetPreview(){
    ContactSheet(

//        AssetUiState.Success(
//            listOf(
//                TokenAsset(
//                    chainId = 10,
//                    name = "Optimism",
//                    symbol = "ETH",
//                    balance = 0.23,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//                TokenAsset(
//                    chainId = 1,
//                    name = "Mainnet",
//                    symbol = "ETH",
//                    balance = 1.43,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//                TokenAsset(
//                    chainId = 1,
//                    name = "DAI",
//                    symbol = "ETH",
//                    balance = 123.0,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//            )
//        )
//        ,
//        {},
//        1
    )
}