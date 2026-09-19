package prayit.simplebudget.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import prayit.simplebudget.core.data.createDataStore
import prayit.simplebudget.core.data.dataDirPath
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.dbSetup.AppDatabase
import prayit.simplebudget.core.data.dbSetup.createDatabase

@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase = createDatabase()

    @Provides
    @SingleIn(AppScope::class)
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideNotificationCacheDao(database: AppDatabase): NotificationCacheDao =
        database.notificationCacheDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDataStore(): DataStore<Preferences> {
        return createDataStore("${dataDirPath()}/settings.preferences_pb")
    }
}
