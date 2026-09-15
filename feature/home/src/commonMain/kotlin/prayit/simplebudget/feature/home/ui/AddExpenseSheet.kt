package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.feature.home.state.FinancialTag
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.home_add_button
import simplebudget.core.resources.generated.resources.home_add_expense_title
import simplebudget.core.resources.generated.resources.home_dialog_cancel
import simplebudget.core.resources.generated.resources.home_dialog_ok
import simplebudget.core.resources.generated.resources.home_expense_amount_label
import simplebudget.core.resources.generated.resources.home_expense_tag_label
import simplebudget.core.resources.generated.resources.home_expense_title_label
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddExpenseSheet(
    title: String,
    amount: String,
    selectedTag: FinancialTag,
    selectedDate: LocalDate,
    onTitleChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTagSelected: (FinancialTag) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onConfirmAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.home_dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.home_dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = LocalAppSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg)
                .padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.home_add_expense_title),
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                label = { Text(stringResource(Res.string.home_expense_title_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChanged,
                label = { Text(stringResource(Res.string.home_expense_amount_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$ ") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(Res.string.home_expense_tag_label),
                style = MaterialTheme.typography.labelLarge,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                FinancialTag.entries.forEach { tag ->
                    TagChip(
                        tag = tag,
                        selected = tag == selectedTag,
                        onClick = { onTagSelected(tag) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.home_dialog_cancel))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatDate(
                            selectedDate.day,
                            selectedDate.month.number, selectedDate.year
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = spacing.md, vertical = spacing.sm),
                    )

                    TextButton(
                        onClick = onConfirmAdd,
                        enabled = title.isNotBlank() && amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                    ) {
                        Text(stringResource(Res.string.home_add_button))
                    }
                }
            }
        }
    }
}

@PhonePreviews
@Composable
private fun AddExpenseSheetPreview() {
    MParafiaTheme {
        AddExpenseSheet(
            title = "Groceries",
            amount = "45.99",
            selectedTag = FinancialTag.Groceries,
            selectedDate = LocalDate(2026, 1, 3),
            onTitleChanged = {},
            onAmountChanged = {},
            onTagSelected = {},
            onDateSelected = {},
            onConfirmAdd = {},
            onDismiss = {},
        )
    }
}
