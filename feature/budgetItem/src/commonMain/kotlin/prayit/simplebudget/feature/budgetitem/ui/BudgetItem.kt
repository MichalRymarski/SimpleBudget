package prayit.simplebudget.feature.budgetitem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.core.utils.TabletPreviews
import prayit.simplebudget.feature.budgetitem.state.BudgetItemState
import prayit.simplebudget.feature.budgetitem.state.BudgetItemViewModel

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
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()

    LaunchedEffect(isDeleted) {
        if (isDeleted) onBack()
    }

    BudgetItemContent(
        state = state,
        showDeleteDialog = showDeleteDialog,
        deviceClass = deviceClass,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onAmountChanged = viewModel::onAmountChanged,
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
    showDeleteDialog: Boolean = false,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onAmountChanged: (String) -> Unit = {},
    onSaveChanges: () -> Unit = {},
    onDeleteRequest: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
) {
    val content = state as? BudgetItemState.Content

    if (showDeleteDialog && content != null) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("Delete expense") },
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
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            Lucide.Trash2,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (content != null && content.hasChanges) {
                FloatingActionButton(
                    onClick = onSaveChanges,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Lucide.Check, contentDescription = "Save changes")
                }
            }
        },
    ) { padding ->
        when (state) {
            is BudgetItemState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            is BudgetItemState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.tag,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                }
            }
            is BudgetItemState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Expense not found",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
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
                hasChanges = false,
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
            ),
            deviceClass = DeviceClass.TabletPortrait,
        )
    }
}
