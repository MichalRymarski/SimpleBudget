package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.feature.home.state.FinancialTag

@Composable
internal fun TagChip(
    tag: FinancialTag,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) tag.color else Color.Transparent
    val spacing = LocalAppSpacing.current

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = if (selected) spacing.xxs else spacing.none,
                color = borderColor,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tag.icon()
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@PhonePreviews
@Composable
private fun TagChipPreview() {
    MParafiaTheme {
        TagChip(tag = FinancialTag.Groceries, selected = true, onClick = {})
    }
}
