package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.PhonePreviews
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.home_spent_label
import simplebudget.core.resources.generated.resources.home_vs_last_month

@Composable
internal fun TotalsRow(
    totalSpent: Double,
    previousMonthTotal: Double,
) {
    val difference = totalSpent - previousMonthTotal
    val spacing = LocalAppSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.home_spent_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatCurrency(totalSpent),
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        if (difference != 0.0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.home_vs_last_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatSigned(difference),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (difference > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF4CAF50)
                    },
                )
            }
        }
    }
}

@PhonePreviews
@Composable
private fun TotalsRowPreview() {
    MParafiaTheme {
        TotalsRow(totalSpent = 1234.56, previousMonthTotal = 1178.32)
    }
}
