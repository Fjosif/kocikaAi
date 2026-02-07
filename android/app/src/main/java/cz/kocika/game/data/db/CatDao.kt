package cz.kocika.game.data.db

import androidx.room.*
import cz.kocika.game.model.CatState
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cat_state WHERE id = 0")
    fun getCatState(): Flow<CatState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCatState(catState: CatState)

    @Query("SELECT * FROM app_settings WHERE id = 0")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: AppSettings)
}
