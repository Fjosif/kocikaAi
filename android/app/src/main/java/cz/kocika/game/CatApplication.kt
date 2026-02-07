package cz.kocika.game

import android.app.Application
import cz.kocika.game.data.db.AppDatabase
import cz.kocika.game.data.repository.CatRepository

class CatApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CatRepository(database.catDao()) }
}
