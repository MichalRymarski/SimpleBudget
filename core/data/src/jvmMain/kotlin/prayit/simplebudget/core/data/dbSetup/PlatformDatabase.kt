package prayit.simplebudget.core.data.dbSetup

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = "simplebudget.db",
        factory = ::AppDatabase_Impl,
    )
}
