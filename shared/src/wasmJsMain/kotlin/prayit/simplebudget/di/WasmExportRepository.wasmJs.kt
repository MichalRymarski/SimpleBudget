package prayit.simplebudget.di

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.export.CsvGenerator
import prayit.simplebudget.export.generateSingleMonthXlsx
import prayit.simplebudget.export.generateXlsx
import prayit.simplebudget.export.shareCsvFile
import prayit.simplebudget.export.shareTextFile
import prayit.simplebudget.export.shareXlsxFile

@ContributesBinding(AppScope::class, binding = binding<ExportRepository>())
@SingleIn(AppScope::class)
@Inject
class WasmExportRepository : ExportRepository {

    override suspend fun exportMonthCsv(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
    ): Result<Unit> {
        val month = Month.entries.getOrNull(monthNumber - 1)
            ?: return Result.failure(IllegalArgumentException("Invalid month: $monthNumber/$year"))
        val csv = CsvGenerator.generateSingleMonth(expenses, month, year)
        val fileName = monthFileName(monthNumber, year, "csv")
        return shareCsvFile(fileName, csv, monthFileName(monthNumber, year, null))
    }

    override suspend fun exportMonthXlsx(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
    ): Result<Unit> {
        val month = Month.entries.getOrNull(monthNumber - 1)
            ?: return Result.failure(IllegalArgumentException("Invalid month: $monthNumber/$year"))
        return generateSingleMonthXlsx(expenses, month, year)
            .mapCatching { xlsx ->
                shareXlsxFile(
                    monthFileName(monthNumber, year, "xlsx"),
                    xlsx,
                    monthFileName(monthNumber, year, null)
                )
                    .getOrThrow()
            }
    }

    override suspend fun exportNotificationsJson(items: List<String>): Result<Unit> =
        shareTextFile(
            "notifications.json",
            "[${items.joinToString(",")}]",
            "Notification debug log",
            "application/json"
        )

    override suspend fun exportHistoryXlsx(expenses: List<Expense>): Result<Unit> =
        generateXlsx(expenses).mapCatching { xlsx ->
            shareXlsxFile("Budget-history.xlsx", xlsx, "Budget history").getOrThrow()
        }

    override fun supportsAutoCapture(): Boolean = false

    override fun isAutoCaptureEnabled(): Boolean = false

    override fun openAutoCaptureSettings() = Unit

    private fun monthFileName(monthNumber: Int, year: Int, extension: String?): String {
        val base = "Budget-${monthNumber.toString().padStart(2, '0')}.$year"
        return if (extension == null) base else "$base.$extension"
    }
}
