package prayit.simplebudget.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import prayit.simplebudget.core.components.navigation.BaseScreen
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.core.utils.DeviceClass
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.core.utils.PhonePreviews
import prayit.simplebudget.core.utils.TabletPreviews
import prayit.simplebudget.feature.home.state.ExpenseItem
import prayit.simplebudget.feature.home.state.FinancialTag
import prayit.simplebudget.feature.home.state.HomeState
import prayit.simplebudget.feature.home.state.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    HomeContent(
        state = state,
        deviceClass = deviceClass,
        onBack = onBack,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onAddClick = viewModel::onToggleAddSheet,
        onExportMonth = viewModel::onExportMonth,
        onExportHistory = viewModel::onExportHistory,
        onTitleChanged = viewModel::onTitleChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onTagSelected = viewModel::onTagSelected,
        onDateSelected = viewModel::onDateSelected,
        onConfirmAdd = viewModel::onConfirmAdd,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState = HomeState.Content(),
    deviceClass: DeviceClass = DeviceClass.PhonePortrait,
    onBack: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onExportMonth: () -> Unit = {},
    onExportHistory: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onAmountChanged: (String) -> Unit = {},
    onTagSelected: (FinancialTag) -> Unit = {},
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit = {},
    onConfirmAdd: () -> Unit = {},
) {
    val content = state as? HomeState.Content ?: return

    if (content.showAddSheet) {
        AddExpenseSheet(
            title = content.title,
            amount = content.amount,
            selectedTag = content.selectedTag,
            selectedDate = content.selectedDate,
            onTitleChanged = onTitleChanged,
            onAmountChanged = onAmountChanged,
            onTagSelected = onTagSelected,
            onDateSelected = onDateSelected,
            onConfirmAdd = onConfirmAdd,
            onDismiss = onAddClick,
        )
    }

    BaseScreen(
        deviceClass = deviceClass,
        items = emptyList(),
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            TotalsRow(
                totalSpent = content.totalSpent,
                previousMonthTotal = content.previousMonthTotal,
            )

            val listState = rememberLazyListState()

            LaunchedEffect(content.items.size) {
                if (content.items.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(content.items, key = { it.id }) { item ->
                    ExpenseItemRow(item = item)
                }
            }

            MonthBar(
                month = content.currentMonth,
                year = content.currentYear,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onAddClick = onAddClick,
                onExportMonth = onExportMonth,
                onExportHistory = onExportHistory,
            )
        }
    }
}

@PhonePreviews
@Composable
fun HomeContentPreview() {
    MParafiaTheme {
        HomeContent(
            state = HomeState.Content(
                currentMonth = Month.January,
                currentYear = 2026,
                totalSpent = 1234.56,
                previousMonthTotal = 1178.32,
                items = listOf(
                    ExpenseItem(
                        id = "1",
                        title = "Groceries",
                        amount = 45.99,
                        date = kotlinx.datetime.LocalDate(2026, 1, 3),
                        tag = FinancialTag.Groceries,
                    ),
                    ExpenseItem(
                        id = "2",
                        title = "Electric bill",
                        amount = 120.00,
                        date = kotlinx.datetime.LocalDate(2026, 1, 1),
                        tag = FinancialTag.Bills,
                    ),
                ),
            ),
        )
    }
}

@TabletPreviews
@Composable
fun HomeContentTabletPreview() {
    MParafiaTheme {
        HomeContent(
            state = HomeState.Content(
                currentMonth = Month.January,
                currentYear = 2026,
                totalSpent = 1234.56,
                previousMonthTotal = 1178.32,
            ),
            deviceClass = DeviceClass.TabletPortrait,
        )
    }
}
