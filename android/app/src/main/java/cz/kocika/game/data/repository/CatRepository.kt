package cz.kocika.game.data.repository

import cz.kocika.game.data.db.CatDao
import cz.kocika.game.model.AppSettings
import cz.kocika.game.model.CatState
import kotlinx.coroutines.flow.Flow

class CatRepository(private val catDao: CatDao) {
    val catState: Flow<CatState?> = catDao.getCatState()
    val settings: Flow<AppSettings?> = catDao.getSettings()

    suspend fun updateCatState(catState: CatState) {
        catDao.updateCatState(catState)
    }

    suspend fun updateSettings(settings: AppSettings) {
        catDao.updateSettings(settings)
    }
}
