package prayit.simplebudget.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import prayit.simplebudget.core.data.entity.NotificationCacheEntity

@Dao
interface NotificationCacheDao {
    /**
     * Atomic check-and-insert: the cache id is derived deterministically from
     * amount+date (see [dedupId]), so a duplicate hits the primary key and is
     * ignored. Concurrent callers cannot both pass the gate — no lock needed,
     * works across threads and processes.
     *
     * @return row id, or -1 when skipped as duplicate.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: NotificationCacheEntity): Long

    @Query("DELETE FROM notification_cache WHERE capturedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM notification_cache")
    suspend fun purgeAll()
}
