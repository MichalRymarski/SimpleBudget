package prayit.simplebudget.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_cache")
data class NotificationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val tag: String,
    val capturedAt: Long,
)
