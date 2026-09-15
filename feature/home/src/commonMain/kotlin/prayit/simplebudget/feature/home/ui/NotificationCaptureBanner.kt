package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.PhonePreviews
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.home_notification_dismiss
import simplebudget.core.resources.generated.resources.home_notification_message
import simplebudget.core.resources.generated.resources.home_notification_open_settings

@Composable
internal fun NotificationCaptureBanner(
    onOpenSettings: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val spacing = LocalAppSpacing.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.home_notification_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.home_notification_dismiss))
            }
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(Res.string.home_notification_open_settings))
            }
        }
    }
}

@PhonePreviews
@Composable
private fun NotificationCaptureBannerPreview() {
    MParafiaTheme {
        NotificationCaptureBanner()
    }
}
