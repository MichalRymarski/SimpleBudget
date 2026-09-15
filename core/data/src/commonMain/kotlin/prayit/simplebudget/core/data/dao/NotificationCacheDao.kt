package prayit.simplebudget.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import prayit.simplebudget.core.data.entity.NotificationCacheEntity

@Dao
interface NotificationCacheDao {
    @Insert
    suspend fun insert(entry: NotificationCacheEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM notification_cache WHERE title = :title AND amount = :amount AND date = :date AND tag = :tag)")
    suspend fun existsDuplicate(title: String, amount: Double, date: Long, tag: String): Boolean

    @Query("DELETE FROM notification_cache WHERE capturedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM notification_cache")
    suspend fun purgeAll()
}
