package prayit.simplebudget.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import prayit.simplebudget.core.data.dao.ExpenseDao
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
}
