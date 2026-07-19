package dk.rpix.wordle

import android.app.Application
import dk.rpix.wordle.data.AppDatabase
import dk.rpix.wordle.data.GameStateRepository
import dk.rpix.wordle.data.WordRepository

class WordleApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val wordRepository by lazy { WordRepository(database.wordDao()) }
    val gameStateRepository by lazy { GameStateRepository(database.gameStateDao()) }
}
