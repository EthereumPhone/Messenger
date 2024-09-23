package org.ethereumphone.settings

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ethereumhpone.messenger.ui.theme.MessengerTheme

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
}

@Composable
private fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text("Settings")
        },

        text = {
            when(settingsUiState) {
                SettingsUiState.Loading -> {
                    Text("Loading")
                }

                is SettingsUiState.Success -> {
                    SettingsPanel(settingsUiState.settings) {}

                }
            }
        },

        confirmButton = {

        }
    )
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings,
    onChangeUseXmtp: (Boolean) -> Unit,
) {

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
            )
        )
    }

}


@Preview
@Composable
private fun PreviewSettingsLoadingDialog() {
    MessengerTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = SettingsUiState.Loading
        )
    }
}







