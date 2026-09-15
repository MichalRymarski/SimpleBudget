package prayit.simplebudget.feature.budgetitem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import org.jetbrains.compose.resources.stringResource
import prayit.simplebudget.core.components.button.fab.AppFloatingActionButton
import prayit.simplebudget.core.components.navigation.BaseScreen
import prayit.simplebudget.core.components.theme.LocalAppSpacing
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.core.utils.TabletPreviews
import prayit.simplebudget.feature.budgetitem.state.BudgetItemState
import prayit.simplebudget.feature.budgetitem.state.BudgetItemViewModel
import prayit.simplebudget.feature.budgetitem.state.ExpenseTag
import simplebudget.core.resources.generated.resources.Res
import simplebudget.core.resources.generated.resources.budgetitem_back
import simplebudget.core.resources.generated.resources.budgetitem_cancel
import simplebudget.core.resources.generated.resources.budgetitem_delete
import simplebudget.core.resources.generated.resources.budgetitem_delete_title
import simplebudget.core.resources.generated.resources.budgetitem_loading
import simplebudget.core.resources.generated.resources.budgetitem_not_found
import simplebudget.core.resources.generated.resources.budgetitem_save_changes
import simplebudget.core.resources.generated.resources.budgetitem_tag_label

@Composable
fun BudgetItemScreen(
    viewModel: BudgetItemViewModel,
    id: String,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
) {
    LaunchedEffect(id) {
        viewModel.load(id)
    }

    val state by viewModel.state.collectAsState()
    val isDeleted = (state as? BudgetItemState.Content)?.isDeleted == true

    LaunchedEffect(isDeleted) {
        if (isDeleted) onBack()
    }

    BudgetItemContent(
        state = state,
        deviceClass = deviceClass,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onTagSelected = viewModel::onTagSelected,
        onSaveChanges = viewModel::onSaveChanges,
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItemContent(
    state: BudgetItemState = BudgetItemState.Loading,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onAmountChanged: (String) -> Unit = {},
    onTagSelected: (ExpenseTag) -> Unit = {},
    onSaveChanges: () -> Unit = {},
    onDeleteRequest: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
) {
    val content = state as? BudgetItemState.Content
    val spacing = LocalAppSpacing.current

    if (content?.showDeleteDialog == true) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text(stringResource(Res.string.budgetitem_delete_title)) },
            text = {
                Text(buildAnnotatedString {
                    append("Delete ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(content.title)
                    }
                    append("?")
                })
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text(stringResource(Res.string.budgetitem_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text(stringResource(Res.string.budgetitem_cancel))
                }
            },
        )
    }

    BaseScreen(
        deviceClass = deviceClass,
        items = emptyList(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Lucide.ArrowLeft,
                            contentDescription = stringResource(Res.string.budgetitem_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            Lucide.Trash2,
                            contentDescription = stringResource(Res.string.budgetitem_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (content?.hasChanges == true) {
                AppFloatingActionButton(
                    deviceClass = deviceClass,
                    onClick = onSaveChanges,
                ) {
                    Icon(
                        Lucide.Check,
                        contentDescription = stringResource(Res.string.budgetitem_save_changes)
                    )
                }
            }
        },
    ) { modifier ->
        when (state) {
            is BudgetItemState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.budgetitem_loading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            is BudgetItemState.Content -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    OutlinedTextField(
                        value = state.editTitle,
                        onValueChange = onTitleChanged,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.date.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    OutlinedTextField(
                        value = state.editAmount,
                        onValueChange = onAmountChanged,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("$ ") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = stringResource(Res.string.budgetitem_tag_label),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ExpenseTag.entries.forEach { tag ->
                            BudgetItemTagChip(
                                tag = tag,
                                selected = tag == state.editTag,
                                onClick = { onTagSelected(tag) },
                            )
                        }
                    }
                }
            }
            is BudgetItemState.NotFound -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.budgetitem_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun BudgetItemTagChip(
    tag: ExpenseTag,
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
private fun BudgetItemTagChipPreview() {
    MParafiaTheme {
        BudgetItemTagChip(tag = ExpenseTag.Groceries, selected = true, onClick = {})
    }
}

@PhonePreviews
@Composable
fun BudgetItemContentPreview() {
    MParafiaTheme {
        BudgetItemContent(
            state = BudgetItemState.Content(
                id = "1",
                title = "Groceries",
                amount = 45.99,
                date = kotlinx.datetime.LocalDate(2026, 1, 3),
                tag = "Groceries",
                editTitle = "Groceries",
                editAmount = "45.99",
                editTag = ExpenseTag.Groceries,
                hasChanges = false,
            ),
        )
    }
}

@PhonePreviews
@Composable
fun BudgetItemContentEditedPreview() {
    MParafiaTheme {
        BudgetItemContent(
            state = BudgetItemState.Content(
                id = "1",
                title = "Groceries",
                amount = 45.99,
                date = kotlinx.datetime.LocalDate(2026, 1, 3),
                tag = "Groceries",
                editTitle = "Groceries",
                editAmount = "45.99",
                editTag = ExpenseTag.EatingOut,
                hasChanges = true,
            ),
        )
    }
}

@TabletPreviews
@Composable
fun BudgetItemContentTabletPreview() {
    MParafiaTheme {
        BudgetItemContent(
            state = BudgetItemState.Content(
                id = "1",
                title = "Groceries",
                amount = 45.99,
                date = kotlinx.datetime.LocalDate(2026, 1, 3),
                tag = "Groceries",
                editTitle = "Groceries",
                editAmount = "45.99",
                editTag = ExpenseTag.Groceries,
            ),
            deviceClass = DeviceClass.TabletPortrait,
        )
    }
}
