package prayit.simplebudget.core.data.dbSetup

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.data.entity.NotificationCacheEntity

@Database(
    entities = [ExpenseEntity::class, NotificationCacheEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun notificationCacheDao(): NotificationCacheDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
