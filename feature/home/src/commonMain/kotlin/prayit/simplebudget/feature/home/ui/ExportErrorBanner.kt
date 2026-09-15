package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.background
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
import simplebudget.core.resources.generated.resources.home_export_error_dismiss

@Composable
internal fun ExportErrorBanner(
    message: String,
    onDismiss: () -> Unit = {},
) {
    val spacing = LocalAppSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.home_export_error_dismiss))
        }
    }
}

@PhonePreviews
@Composable
private fun ExportErrorBannerPreview() {
    MParafiaTheme {
        ExportErrorBanner(message = "Export failed")
    }
}
