package prayit.simplebudget.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import prayit.simplebudget.core.data.entity.NotificationDebugEntity

@Dao
interface NotificationDebugDao {
    @Insert
    suspend fun insert(entry: NotificationDebugEntity)

    @Query("SELECT dataJson FROM notification_debug ORDER BY id ASC")
    suspend fun getAllJson(): List<String>

    @Query("DELETE FROM notification_debug")
    suspend fun clear()
}
