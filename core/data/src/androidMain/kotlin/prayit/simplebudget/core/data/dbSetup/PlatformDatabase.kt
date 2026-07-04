package prayit.simplebudget.core.data.dbSetup

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = applicationContext
    val dbFile = context.getDatabasePath("simplebudget.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}

object AppContext {
    lateinit var instance: Context
}

private val applicationContext: Context
    get() = AppContext.instance
