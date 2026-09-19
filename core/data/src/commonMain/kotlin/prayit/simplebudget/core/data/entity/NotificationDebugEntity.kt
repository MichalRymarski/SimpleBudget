package prayit.simplebudget.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_debug")
data class NotificationDebugEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataJson: String,
)
