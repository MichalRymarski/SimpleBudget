package prayit.simplebudget.core.data.dbSetup

import androidx.room.Room
import androidx.room.RoomDatabase
import prayit.simplebudget.core.utils.AppContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AppContext.requireInstance()
    val dbFile = context.getDatabasePath("simplebudget.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
