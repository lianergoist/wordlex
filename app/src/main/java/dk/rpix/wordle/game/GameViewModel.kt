package dk.rpix.wordle.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.rpix.wordle.data.GameState
import dk.rpix.wordle.data.GameStateRepository
import dk.rpix.wordle.data.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val targetWord: String = "",
    val guesses: List<List<EvaluatedLetter>> = emptyList(),
    val currentGuess: String = "",
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val message: String? = null,
    val keyboardState: Map<Char, LetterStatus> = emptyMap(),
    val possibleWordsCount: Int = 0,
    val hintWord: String? = null
)

class GameViewModel(
    private val wordRepository: WordRepository,
    private val gameStateRepository: GameStateRepository
) : ViewModel() {

    private val engine = WordleGameEngine()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadOrStartGame()
    }

    private fun loadOrStartGame() {
        viewModelScope.launch {
            val savedState = gameStateRepository.getGameState()
            if (savedState != null && !savedState.isGameOver) {
                val evaluatedGuesses = savedState.guesses.map { engine.evaluateGuess(it, savedState.targetWord) }
                _uiState.update {
                    it.copy(
                        targetWord = savedState.targetWord,
                        guesses = evaluatedGuesses,
                        isGameOver = savedState.isGameOver,
                        isWon = savedState.isWon
                    )
                }
                updateKeyboardState()
                updatePossibleWordsCount()
            } else {
                startNewGame()
            }
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            val randomWord = wordRepository.getRandomWord()?.word ?: "apple"
            _uiState.value = GameUiState(targetWord = randomWord)
            gameStateRepository.clearGameState()
            updatePossibleWordsCount()
        }
    }

    fun onLetterInput(char: Char) {
        if (_uiState.value.isGameOver || _uiState.value.currentGuess.length >= 5) return
        _uiState.update { it.copy(currentGuess = it.currentGuess + char.uppercaseChar(), message = null) }
    }

    fun onDelete() {
        if (_uiState.value.isGameOver || _uiState.value.currentGuess.isEmpty()) return
        _uiState.update { it.copy(currentGuess = it.currentGuess.dropLast(1), message = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isGameOver || state.currentGuess.length < 5) return

        viewModelScope.launch {
            val guess = state.currentGuess.lowercase()
            if (!wordRepository.isWordValid(guess)) {
                _uiState.update { it.copy(message = "Not in word list") }
                return@launch
            }

            val evaluated = engine.evaluateGuess(guess, state.targetWord)
            val newGuesses = state.guesses + listOf(evaluated)
            val isWon = guess == state.targetWord
            val isGameOver = isWon || newGuesses.size >= 6

            _uiState.update {
                it.copy(
                    guesses = newGuesses,
                    currentGuess = "",
                    isWon = isWon,
                    isGameOver = isGameOver,
                    message = when {
                        isWon -> "Splendid!"
                        isGameOver -> "The word was ${state.targetWord.uppercase()}"
                        else -> null
                    }
                )
            }

            updateKeyboardState()
            updatePossibleWordsCount()
            saveGameState()
        }
    }

    private fun updateKeyboardState() {
        val newKeyboardState = mutableMapOf<Char, LetterStatus>()
        _uiState.value.guesses.flatten().forEach { evaluated ->
            val char = evaluated.char.uppercaseChar()
            val currentStatus = newKeyboardState[char]
            if (currentStatus == null || shouldUpdateStatus(currentStatus, evaluated.status)) {
                newKeyboardState[char] = evaluated.status
            }
        }
        _uiState.update { it.copy(keyboardState = newKeyboardState) }
    }

    private fun shouldUpdateStatus(current: LetterStatus, new: LetterStatus): Boolean {
        return when (current) {
            LetterStatus.EMPTY -> true
            LetterStatus.ABSENT -> new != LetterStatus.EMPTY
            LetterStatus.PRESENT -> new == LetterStatus.CORRECT
            LetterStatus.CORRECT -> false
        }
    }

    private suspend fun updatePossibleWordsCount() {
        val allWords = wordRepository.getAllWords().first()
        val possible = allWords.filter { engine.isPossibleWord(it.word, _uiState.value.guesses) }
        _uiState.update { it.copy(possibleWordsCount = possible.size) }
    }

    fun requestHint() {
        viewModelScope.launch {
            val allWords = wordRepository.getAllWords().first()
            val possible = allWords.filter { engine.isPossibleWord(it.word, _uiState.value.guesses) }
            if (possible.isNotEmpty()) {
                val hint = possible.random().word.uppercase()
                _uiState.update { it.copy(message = "Try: $hint") }
            }
        }
    }

    fun revealLetterHint() {
        val state = _uiState.value
        if (state.isGameOver) return
        
        val target = state.targetWord.uppercase()
        val knownCorrect = MutableList(5) { ' ' }
        state.guesses.forEach { guess ->
            guess.forEachIndexed { index, evaluated ->
                if (evaluated.status == LetterStatus.CORRECT) {
                    knownCorrect[index] = evaluated.char.uppercaseChar()
                }
            }
        }

        val unknownIndices = knownCorrect.mapIndexedNotNull { index, c -> if (c == ' ') index else null }
        if (unknownIndices.isNotEmpty()) {
            val revealIndex = unknownIndices.random()
            val charToReveal = target[revealIndex]
            _uiState.update { it.copy(message = "Hint: '${charToReveal}' at position ${revealIndex + 1}") }
        } else {
            _uiState.update { it.copy(message = "You already know all letters!") }
        }
    }

    private fun saveGameState() {
        viewModelScope.launch {
            val state = _uiState.value
            gameStateRepository.saveGameState(
                GameState(
                    targetWord = state.targetWord,
                    guesses = state.guesses.map { evaluatedList -> evaluatedList.map { it.char }.joinToString("") },
                    isGameOver = state.isGameOver,
                    isWon = state.isWon
                )
            )
        }
    }
}
