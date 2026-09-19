package prayit.simplebudget.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import prayit.simplebudget.core.components.navigation.SnackbarMessage
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.feature.settings.state.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
    onSnackbarMessage: (SnackbarMessage) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            onSnackbarMessage(message)
        }
    }

    SettingsContent(
        state = state,
        deviceClass = deviceClass,
        onBack = onBack,
        onRecipientEmailChanged = viewModel::onRecipientEmailChanged,
        onSenderEmailChanged = viewModel::onSenderEmailChanged,
        onAppPasswordChanged = viewModel::onAppPasswordChanged,
        onAutoExportToggle = viewModel::onAutoExportToggle,
        onAutoExportWithManualToggle = viewModel::onAutoExportWithManualToggle,
        onSendTestEmail = viewModel::onSendTestEmail,
    )
}
