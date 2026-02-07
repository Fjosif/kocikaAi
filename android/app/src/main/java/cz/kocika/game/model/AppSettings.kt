package cz.kocika.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 0,
    val parentPin: String = "0000",
    val aiEnabled: Boolean = true,
    val storiesEnabled: Boolean = true,
    val playTimeLimitMinutes: Int = 30
)
