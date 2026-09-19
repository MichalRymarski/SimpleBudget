package prayit.simplebudget.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Send
import prayit.simplebudget.core.components.navigation.BaseScreen
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.feature.settings.state.SettingsState
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.home_previous_month
import simplebudget.core.resources.generated.resources.settings_app_password
import simplebudget.core.resources.generated.resources.settings_app_password_help
import simplebudget.core.resources.generated.resources.settings_auto_export
import simplebudget.core.resources.generated.resources.settings_auto_export_with_manual
import simplebudget.core.resources.generated.resources.settings_recipient_email
import simplebudget.core.resources.generated.resources.settings_send_test_email
import simplebudget.core.resources.generated.resources.settings_sender_email
import simplebudget.core.resources.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState = SettingsState.Content(),
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
    onRecipientEmailChanged: (String) -> Unit = {},
    onSenderEmailChanged: (String) -> Unit = {},
    onAppPasswordChanged: (String) -> Unit = {},
    onAutoExportToggle: (Boolean) -> Unit = {},
    onAutoExportWithManualToggle: (Boolean) -> Unit = {},
    onSendTestEmail: () -> Unit = {},
) {
    val content = state as? SettingsState.Content ?: return
    val spacing = LocalAppSpacing.current

    BaseScreen(
        deviceClass = deviceClass,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Lucide.ChevronLeft,
                            contentDescription = stringResource(Res.string.home_previous_month),
                        )
                    }
                },
            )
        },
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            SaveOnFocusLostTextField(
                value = content.settings.recipientEmail,
                onValueChange = onRecipientEmailChanged,
                label = stringResource(Res.string.settings_recipient_email),
                leadingIcon = { Icon(Lucide.Mail, contentDescription = null) },
                keyboardType = KeyboardType.Email,
            )

            SaveOnFocusLostTextField(
                value = content.settings.senderEmail,
                onValueChange = onSenderEmailChanged,
                label = stringResource(Res.string.settings_sender_email),
                leadingIcon = { Icon(Lucide.Send, contentDescription = null) },
                keyboardType = KeyboardType.Email,
            )

            SaveOnFocusLostTextField(
                value = content.settings.appPassword,
                onValueChange = onAppPasswordChanged,
                label = stringResource(Res.string.settings_app_password),
                leadingIcon = { Icon(Lucide.Mail, contentDescription = null) },
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            val uriHandler = LocalUriHandler.current
            Text(
                text = stringResource(Res.string.settings_app_password_help),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://myaccount.google.com/apppasswords")
                },
            )

            SettingsSwitch(
                label = stringResource(Res.string.settings_auto_export),
                checked = content.settings.autoExportEnabled,
                onCheckedChange = onAutoExportToggle,
            )

            if (content.settings.autoExportEnabled) {
                SettingsSwitch(
                    label = stringResource(Res.string.settings_auto_export_with_manual),
                    checked = content.settings.autoExportWithManual,
                    onCheckedChange = onAutoExportWithManualToggle,
                )

                Button(
                    onClick = onSendTestEmail,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.settings_send_test_email))
                }
            }
        }
    }
}

@Composable
private fun SaveOnFocusLostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    var localValue by remember(value) { mutableStateOf(value) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (localValue != value) localValue = value
    }

    TextField(
        value = localValue,
        onValueChange = { localValue = it },
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Lucide.EyeOff else Lucide.Eye,
                        contentDescription = null,
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
            if (!state.isFocused && localValue != value) {
                onValueChange(localValue)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
    )
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = LocalAppSpacing.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(end = spacing.md),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@PhonePreviews
@Composable
private fun SettingsContentPreview() {
    MParafiaTheme {
        SettingsContent()
    }
}
