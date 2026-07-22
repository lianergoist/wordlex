package dk.rpix.wordle.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dk.rpix.wordle.data.GameStateRepository
import dk.rpix.wordle.data.WordRepository

class GameViewModelFactory(
    private val wordRepository: WordRepository,
    private val gameStateRepository: GameStateRepository,
    private val settingsRepository: dk.rpix.wordle.data.SettingsRepository,
    private val statisticsRepository: dk.rpix.wordle.data.StatisticsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(wordRepository, gameStateRepository, settingsRepository, statisticsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
