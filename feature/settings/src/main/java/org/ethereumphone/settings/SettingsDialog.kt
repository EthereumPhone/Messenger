package org.ethereumphone.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.services.storage.file.PropertyFile.Column
import org.ethereumhpone.messenger.ui.theme.MessengerTheme
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    SettingsDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeUseXmtp = viewModel::updateUseXmtp
    )
}

//@Composable
//private fun SettingsDialog(
//    settingsUiState: SettingsUiState,
//    onDismiss: () -> Unit,
//    onChangeUseXmtp: (Boolean) -> Unit
//) {
//
//    AlertDialog(
//        onDismissRequest = { onDismiss() },
//        title = {
//            Column(
//                modifier = Modifier,
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Text(
//                    fontFamily = Fonts.INTER,
//                    fontWeight = FontWeight.SemiBold,
//                    text = "Settings",
//                    fontSize = 24.sp
//                )
//                //Divider()
//            }
//
//
//        },
//        backgroundColor = Colors.BLACK,
//        contentColor = Colors.WHITE,
//
//
//    text = {
//
//            Column(
//                Modifier
//                    .verticalScroll(rememberScrollState())
//                    .padding(top = 8.dp)) {
//                when(settingsUiState) {
//                    SettingsUiState.Loading -> {
//                        Text("Loading...")
//                    }
//
//                    is SettingsUiState.Success -> {
//                        SettingsPanel(
//                            settingsUiState.settings,
//                            onChangeUseXmtp = onChangeUseXmtp,
//                        )
//                    }
//                }
//            }
//        },
//
//        confirmButton = {
//            Text(
//                text = "OK",
//                modifier = Modifier.clickable { onDismiss() }
//            )
//        }
//    )
//}

@Composable
private fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeUseXmtp: (Boolean) -> Unit
) {

    Dialog(
        onDismissRequest = { onDismiss() },
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Colors.BLACK,
            contentColor = Colors.WHITE,
            shadowElevation = 20.dp,
            border = BorderStroke(width = 1.dp, Colors.WHITE)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)

                ) {

                        Text(
                            text = "Settings",
                            style = TextStyle(
                                fontFamily = Fonts.INTER,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = Colors.WHITE
                            ),

                        )


                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())) {
                        when(settingsUiState) {
                            SettingsUiState.Loading -> {
                                Text(
                                    "Loading...",
                                    style = TextStyle(
                                        fontFamily = Fonts.INTER,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 18.sp,
                                        color = Colors.GRAY
                                    ),

                                )
                            }

                            is SettingsUiState.Success -> {
                                SettingsPanel(
                                    settingsUiState.settings,
                                    onChangeUseXmtp = onChangeUseXmtp,
                                )
                            }
                        }
                    }



                    Text(
                        text = "OK",
                        style = TextStyle(
                            fontFamily = Fonts.INTER,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Colors.WHITE
                        ),
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .width(48.dp)
                            .clickable { onDismiss() }
                    )


                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings,
    onChangeUseXmtp: (Boolean) -> Unit,
) {

    //

    Column(
        verticalArrangement =Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        SettingsSectionTitle("Messaging protocols")
        OptionItem(
            "Use XMTP",
            "XMTP enables secure, sms-free messaging between eth accounts",
            selected = settings.useXmtp,
            onToggle = { onChangeUseXmtp(!settings.useXmtp) }

        )
    }


    /*
    Divider()
    OptionItem(
        "Use SMS",
        "SMS is a text ",
        selected = false,
        onToggle = {  }

    )
     */



}

@Composable
fun OptionItem(
    header: String,
    description: String,
    selected: Boolean,
    onToggle: () -> Unit
) {

        Row(

        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = header,
                    modifier = Modifier.alpha(0.66f),
                    style = TextStyle(
                        fontFamily = Fonts.INTER,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Colors.WHITE
                    ),
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = Fonts.INTER,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Colors.GRAY
                    ),
                )
            }
            Box(modifier = Modifier.weight(0.3f)){
                Switch(
                    selected,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Colors.BLACK,
                        checkedTrackColor = Colors.WHITE,
                        checkedBorderColor = Colors.WHITE,
                        checkedIconColor = Colors.BLACK,
                        uncheckedThumbColor = Colors.WHITE,
                        uncheckedTrackColor = Colors.DARK_GRAY,
                        uncheckedBorderColor = Colors.WHITE,
                        uncheckedIconColor = Colors.WHITE,

                        ),
                )
            }
        }


/*
Switch(
                    selected,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.8f).weight(0.3f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Colors.BLACK,
                        checkedTrackColor = Colors.WHITE,
                        checkedBorderColor = Colors.WHITE,
                        checkedIconColor = Colors.BLACK,
                        uncheckedThumbColor = Colors.WHITE,
                        uncheckedTrackColor = Colors.DARK_GRAY,
                        uncheckedBorderColor = Colors.WHITE,
                        uncheckedIconColor = Colors.WHITE,

                        ),
                )
 */

}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = Fonts.INTER,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Colors.WHITE
        ),
    )
}

@Preview
@Composable
private fun PreviewSettingsDialog() {
    MessengerTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = SettingsUiState.Success(
                UserEditableSettings(
                    ringtone = "",
                    useXmtp = false
                )
            ),
            onChangeUseXmtp = {}
        )
    }

}


@Preview
@Composable
private fun PreviewSettingsLoadingDialog() {
    MessengerTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = SettingsUiState.Loading,
            onChangeUseXmtp = {}
        )
    }
}







