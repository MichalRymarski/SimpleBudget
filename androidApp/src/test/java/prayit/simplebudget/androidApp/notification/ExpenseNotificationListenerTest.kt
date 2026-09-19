package prayit.simplebudget.androidApp.notification

import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.export.NotificationAccess
import java.util.Collections
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseNotificationListenerTest {

    private class FakePendingExpenseRepository : PendingExpenseRepository {
        val staged = Collections.synchronizedList(mutableListOf<Expense>())
        val rawLog = Collections.synchronizedList(mutableListOf<String>())
        override suspend fun stageAndCommit(expense: Expense) {
            staged += expense
        }

        override suspend fun purgeExpired(nowEpochMillis: Long) = Unit

        override suspend fun logRawNotification(json: String) {
            rawLog += json
        }

        override suspend fun getRawNotifications(): List<String> = rawLog.toList()
    }

    private val fake = FakePendingExpenseRepository()

    @Suppress("DEPRECATION")
    private fun postNotification(packageName: String, title: String, text: String) {
        val context = RuntimeEnvironment.getApplication()
        val notification = NotificationCompat.Builder(context, "test-channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        val sbn = StatusBarNotification(
            packageName,
            packageName,
            1,
            "test-tag",
            Process.myUid(),
            0,
            0,
            notification,
            Process.myUserHandle(),
            System.currentTimeMillis(),
        )
        val service =
            Robolectric.buildService(ExpenseNotificationListener::class.java).create().get()
        service.repositoryOverride = fake
        service.onNotificationPosted(sbn)
    }

    private fun awaitStaged(
        predicate: (Expense) -> Boolean,
        timeout: Long = 10_000L,
    ): Expense = runBlocking {
        withTimeout(timeout.milliseconds) {
            while (fake.staged.none(predicate)) {
                delay(100.milliseconds)
            }
            fake.staged.first(predicate)
        }
    }

    @Test
    fun revolutPaymentNotificationIsStagedAsExpense() {
        postNotification(
            "com.revolut.revolut",
            "Revolut",
            "Paid €9.99 at Steamgames.com Spent today: €9.99",
        )
        val staged = awaitStaged({ it.amount == 9.99 })
        assertEquals("Technology", staged.tag)
    }

    @Test
    fun walletPaymentNotificationIsStagedAsExpense() {
        postNotification(
            "com.google.android.apps.walletnfcrel",
            "Google Wallet",
            "Purchase $12.50 at Starbucks Downtown",
        )
        val staged = awaitStaged({ it.amount == 12.50 })
        assertEquals("EatingOut", staged.tag)
    }

    @Test
    fun unsupportedPackageIsIgnored() {
        postNotification("com.random.app", "Hi", "Paid $77.71 at Nowhere")
        runBlocking {
            delay(1_500.milliseconds)
            assertTrue(fake.staged.none { it.amount == 77.71 })
        }
    }

    @Test
    fun otpNotificationIsIgnored() {
        postNotification(
            "com.revolut.revolut",
            "Revolut",
            "Your verification OTP code is 483920",
        )
        runBlocking {
            delay(1_500.milliseconds)
            assertTrue(fake.staged.isEmpty())
        }
    }

    @Test
    fun allNotificationsAreLoggedAsJson() {
        postNotification("com.random.app", "Some title", "Some body text")
        runBlocking {
            withTimeout(10_000.milliseconds) {
                while (fake.rawLog.isEmpty()) {
                    delay(100.milliseconds)
                }
            }
            val logged = fake.rawLog.first()
            assertTrue(logged.contains("com.random.app"))
            assertTrue(logged.contains("Some title"))
            assertTrue(logged.contains("Some body text"))
        }
    }

    @Test
    fun listenerAccessDisabledByDefault() {
        assertFalse(
            NotificationAccess.isListenerEnabled(
                RuntimeEnvironment.getApplication(),
                "prayit.simplebudget.androidApp",
                "prayit.simplebudget.androidApp.notification.ExpenseNotificationListener",
            ),
        )
    }
}
