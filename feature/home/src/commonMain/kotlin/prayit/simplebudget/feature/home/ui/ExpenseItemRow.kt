package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.feature.home.state.ExpenseItem
import prayit.simplebudget.feature.home.state.FinancialTag

@Composable
internal fun ExpenseItemRow(
    item: ExpenseItem,
    onClick: () -> Unit = {},
) {
    val spacing = LocalAppSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(item.tag.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            item.tag.icon()
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = item.tag.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(item.amount),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = padZero(item.date.day),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PhonePreviews
@Composable
private fun ExpenseItemRowPreview() {
    MParafiaTheme {
        ExpenseItemRow(
            item = ExpenseItem(
                id = "1",
                title = "Groceries",
                amount = 45.99,
                date = LocalDate(2026, 1, 3),
                tag = FinancialTag.Groceries,
            ),
        )
    }
}
