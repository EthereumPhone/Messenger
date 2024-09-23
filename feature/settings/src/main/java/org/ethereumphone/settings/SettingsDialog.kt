package org.ethereumphone.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.services.storage.file.PropertyFile.Column
import org.ethereumhpone.messenger.ui.theme.MessengerTheme

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

@Composable
private fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeUseXmtp: (Boolean) -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Settings",
                fontSize = 24.sp
            )
        },

        text = {
            Divider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when(settingsUiState) {
                    SettingsUiState.Loading -> {
                        Text("Loading...")
                    }

                    is SettingsUiState.Success -> {
                        SettingsPanel(
                            settingsUiState.settings,
                            onChangeUseXmtp = onChangeUseXmtp,
                        )
                    }
                }
            }
        },

        confirmButton = {
            Text(
                text = "OK",
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    )
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings,
    onChangeUseXmtp: (Boolean) -> Unit,
) {

    //

    SettingsSectionTitle("Messaging protocols")
    OptionItem(
        "Use XMTP",
        "XMTP enables secure, sms-free messaging between blockchain accounts",
        selected = settings.useXmtp,
        onToggle = { onChangeUseXmtp(!settings.useXmtp) }

    )

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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = header,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            selected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.8f)

        )
    }

}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
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







