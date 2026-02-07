package cz.kocika.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_state")
data class CatState(
    @PrimaryKey val id: Int = 0,
    val hunger: Int = 100,
    val energy: Int = 100,
    val hygiene: Int = 100,
    val mood: Int = 100,
    val health: Int = 100,
    val lastUpdated: Long = System.currentTimeMillis()
)
