package prayit.simplebudget.core.data.repository

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.di.AppScope
import prayit.simplebudget.export.CsvGenerator
import prayit.simplebudget.export.generateSingleMonthXlsx
import prayit.simplebudget.export.generateXlsx
import prayit.simplebudget.export.isNotificationCaptureEnabled
import prayit.simplebudget.export.openNotificationCaptureSettings
import prayit.simplebudget.export.shareCsvFile
import prayit.simplebudget.export.shareXlsxFile
import prayit.simplebudget.export.supportsNotificationCapture

@ContributesBinding(AppScope::class)
@Inject
class ExportRepositoryImpl : ExportRepository {

    override suspend fun exportMonthCsv(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
    ): Result<Unit> {
        val month = monthOrNull(monthNumber) ?: return invalidMonth(monthNumber, year)
        val csv = CsvGenerator.generateSingleMonth(expenses, month, year)
        return shareCsvFile(
            monthFileName(monthNumber, year, "csv"),
            csv,
            monthFileName(monthNumber, year, null)
        )
    }

    override suspend fun exportMonthXlsx(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
    ): Result<Unit> {
        val month = monthOrNull(monthNumber) ?: return invalidMonth(monthNumber, year)
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

    override suspend fun exportHistoryXlsx(expenses: List<Expense>): Result<Unit> =
        generateXlsx(expenses).mapCatching { xlsx ->
            shareXlsxFile("Budget-history.xlsx", xlsx, "Budget history").getOrThrow()
        }

    override fun supportsAutoCapture(): Boolean = supportsNotificationCapture()

    override fun isAutoCaptureEnabled(): Boolean = isNotificationCaptureEnabled()

    override fun openAutoCaptureSettings() = openNotificationCaptureSettings()

    private fun monthOrNull(monthNumber: Int): Month? =
        Month.entries.getOrNull(monthNumber - 1)

    private fun invalidMonth(monthNumber: Int, year: Int): Result<Unit> =
        Result.failure(IllegalArgumentException("Invalid month: $monthNumber/$year"))

    private fun monthFileName(monthNumber: Int, year: Int, extension: String?): String {
        val base = "Budget-${monthNumber.toString().padStart(2, '0')}.$year"
        return if (extension == null) base else "$base.$extension"
    }
}
