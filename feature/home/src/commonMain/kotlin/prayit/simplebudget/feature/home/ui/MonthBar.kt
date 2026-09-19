package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share
import org.jetbrains.compose.resources.stringResource
import prayit.simplebudget.core.components.button.fab.AppFloatingActionButton
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.core.utils.PhonePreviews
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.home_add_expense
import simplebudget.core.resources.generated.resources.home_export_content_description
import simplebudget.core.resources.generated.resources.home_export_history_xlsx
import simplebudget.core.resources.generated.resources.home_export_month_csv
import simplebudget.core.resources.generated.resources.home_export_month_xlsx
import simplebudget.core.resources.generated.resources.home_next_month
import simplebudget.core.resources.generated.resources.home_previous_month
import simplebudget.core.resources.generated.resources.settings_menu_item

@Composable
internal fun MonthBar(
    month: Month,
    year: Int,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddClick: () -> Unit,
    onExportMonth: () -> Unit = {},
    onExportMonthXlsx: () -> Unit = {},
    onExportHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    var showExportMenu by remember { mutableStateOf(false) }
    val spacing = LocalAppSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Lucide.ChevronLeft,
                    contentDescription = stringResource(Res.string.home_previous_month)
                )
            }
            Text(
                text = "${month.stringName} $year",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    Lucide.ChevronRight,
                    contentDescription = stringResource(Res.string.home_next_month)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                IconButton(onClick = { showExportMenu = true }) {
                    Icon(
                        Lucide.Share,
                        contentDescription = stringResource(Res.string.home_export_content_description)
                    )
                }

                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.home_export_month_csv)) },
                        onClick = {
                            showExportMenu = false
                            onExportMonth()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.home_export_month_xlsx)) },
                        onClick = {
                            showExportMenu = false
                            onExportMonthXlsx()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.home_export_history_xlsx)) },
                        onClick = {
                            showExportMenu = false
                            onExportHistory()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.settings_menu_item)) },
                        leadingIcon = { Icon(Lucide.Settings, contentDescription = null) },
                        onClick = {
                            showExportMenu = false
                            onOpenSettings()
                        },
                    )
                }
            }

            AppFloatingActionButton(
                deviceClass = deviceClass,
                onClick = onAddClick,
            ) {
                Icon(Lucide.Plus, contentDescription = stringResource(Res.string.home_add_expense))
            }
        }
    }
}

@PhonePreviews
@Composable
private fun MonthBarPreview() {
    MParafiaTheme {
        MonthBar(
            month = Month.January,
            year = 2026,
            onPreviousMonth = {},
            onNextMonth = {},
            onAddClick = {},
        )
    }
}
