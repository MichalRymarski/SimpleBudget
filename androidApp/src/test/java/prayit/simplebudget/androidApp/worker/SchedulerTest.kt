package prayit.simplebudget.androidApp.worker

import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchedulerTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Calendar =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }

    private fun targetOf(now: Calendar): Calendar =
        (now.clone() as Calendar).apply {
            timeInMillis = now.timeInMillis + millisUntilNextMonthlyExport(now)
        }

    private fun assertNextOccurrence(now: Calendar, year: Int, month: Int, day: Int, hour: Int) {
        val delay = millisUntilNextMonthlyExport(now)
        val target = targetOf(now)
        assertTrue(delay > 0, "delay must be positive for $now")
        assertEquals(year, target[Calendar.YEAR])
        assertEquals(month - 1, target[Calendar.MONTH])
        assertEquals(day, target[Calendar.DAY_OF_MONTH])
        assertEquals(hour, target[Calendar.HOUR_OF_DAY])
        assertEquals(0, target[Calendar.MINUTE])
        assertEquals(0, target[Calendar.SECOND])
    }

    @Test
    fun midMonthBeforeEight_targetsToday() {
        assertNextOccurrence(at(2026, 9, 26, 7, 30), 2026, 10, 1, 8)
    }

    @Test
    fun midMonthAfterEight_targetsNextMonth() {
        assertNextOccurrence(at(2026, 9, 26, 9), 2026, 10, 1, 8)
    }

    @Test
    fun firstOfMonthBeforeEight_targetsToday() {
        assertNextOccurrence(at(2026, 10, 1, 7, 59), 2026, 10, 1, 8)
    }

    @Test
    fun firstOfMonthExactlyAtEight_rollsToNextMonth() {
        // The worker re-arms itself right at (or after) the fire time.
        assertNextOccurrence(at(2026, 10, 1, 8), 2026, 11, 1, 8)
    }

    @Test
    fun firstOfMonthAfterEight_rollsToNextMonth() {
        assertNextOccurrence(at(2026, 10, 1, 8, 1), 2026, 11, 1, 8)
    }

    @Test
    fun newYearsEve_targetsJanuaryOfNextYear() {
        assertNextOccurrence(at(2026, 12, 31, 23, 59), 2027, 1, 1, 8)
    }

    @Test
    fun februaryLeapYear_startOfMonth() {
        assertNextOccurrence(at(2028, 2, 1, 12), 2028, 3, 1, 8)
    }

    @Test
    fun everyHourOfEveryDay_alwaysLandsOnEighthOfFirst() {
        val now = at(2026, 1, 1, 0)
        repeat(24 * 370) {
            val delay = millisUntilNextMonthlyExport(now)
            val target = targetOf(now)
            assertTrue(delay > 0, "delay must be positive at $now")
            assertEquals(1, target.get(Calendar.DAY_OF_MONTH), "target day for $now")
            assertEquals(8, target.get(Calendar.HOUR_OF_DAY), "target hour for $now")
            now.add(Calendar.HOUR_OF_DAY, 1)
        }
    }
}
