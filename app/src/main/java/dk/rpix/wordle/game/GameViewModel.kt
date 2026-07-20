package dk.rpix.wordle.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.rpix.wordle.R
import dk.rpix.wordle.data.GameState
import dk.rpix.wordle.data.GameStateRepository
import dk.rpix.wordle.data.Word
import dk.rpix.wordle.data.WordRepository
import dk.rpix.wordle.ui.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val targetWord: String = "",
    val guesses: List<List<EvaluatedLetter>> = emptyList(),
    val currentGuess: String = "     ",
    val focusedIndex: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val message: UiMessage? = null,
    val keyboardState: Map<Char, LetterStatus> = emptyMap(),
    val possibleWordsCount: Int = 0,
    val hintWord: String? = null,
    val showPossibleWordsDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showImportOptionsDialog: Boolean = false,
    val pendingImportWords: List<String> = emptyList(),
    val matchingWords: List<String> = emptyList()
)

class GameViewModel(
    private val wordRepository: WordRepository,
    private val gameStateRepository: GameStateRepository,
    private val settingsRepository: dk.rpix.wordle.data.SettingsRepository
) : ViewModel() {

    private val engine = WordleGameEngine()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLanguage: String = java.util.Locale.getDefault().language

    init {
        viewModelScope.launch {
            settingsRepository.languageFlow.collect { lang ->
                if (lang != null && lang != currentLanguage) {
                    currentLanguage = lang
                    startNewGame()
                }
            }
        }
        loadOrStartGame()
    }

    private fun loadOrStartGame() {
        viewModelScope.launch {
            val savedState = gameStateRepository.getGameState()
            if (savedState != null && !savedState.isGameOver && savedState.language == currentLanguage) {
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
            val randomWord = wordRepository.getRandomWord(currentLanguage)?.word ?: "apple"
            _uiState.value = GameUiState(targetWord = randomWord)
            gameStateRepository.clearGameState()
            updatePossibleWordsCount()
        }
    }

    fun onLetterInput(char: Char) {
        val state = _uiState.value
        if (state.isGameOver) return
        
        val newGuessArr = state.currentGuess.toCharArray()
        newGuessArr[state.focusedIndex] = char.uppercaseChar()
        val newGuess = String(newGuessArr)
        
        val nextFocus = if (state.focusedIndex < 4) state.focusedIndex + 1 else state.focusedIndex
        
        _uiState.update { 
            it.copy(
                currentGuess = newGuess, 
                focusedIndex = nextFocus,
                message = null
            ) 
        }
    }

    fun onDelete() {
        val state = _uiState.value
        if (state.isGameOver) return
        
        val newGuessArr = state.currentGuess.toCharArray()
        
        if (newGuessArr[state.focusedIndex] != ' ') {
            // If current cell has a letter, clear it
            newGuessArr[state.focusedIndex] = ' '
            _uiState.update { it.copy(currentGuess = String(newGuessArr), message = null) }
        } else if (state.focusedIndex > 0) {
            // If current cell is empty, move back and clear that one
            val nextFocus = state.focusedIndex - 1
            newGuessArr[nextFocus] = ' '
            _uiState.update { 
                it.copy(
                    currentGuess = String(newGuessArr), 
                    focusedIndex = nextFocus,
                    message = null
                ) 
            }
        }
    }

    fun onCellClick(index: Int) {
        if (_uiState.value.isGameOver) return
        _uiState.update { it.copy(focusedIndex = index) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val guessRaw = state.currentGuess.replace(" ", "")
        if (state.isGameOver || guessRaw.length < 5) return

        viewModelScope.launch {
            val guess = state.currentGuess.lowercase()
            if (!wordRepository.isWordValid(guess, currentLanguage)) {
                _uiState.update { it.copy(message = UiMessage(R.string.msg_not_in_word_list)) }
                return@launch
            }

            val evaluated = engine.evaluateGuess(guess, state.targetWord)
            val newGuesses = state.guesses + listOf(evaluated)
            val isWon = guess == state.targetWord
            val isGameOver = isWon || newGuesses.size >= 6

            _uiState.update {
                it.copy(
                    guesses = newGuesses,
                    currentGuess = "     ",
                    focusedIndex = 0,
                    isWon = isWon,
                    isGameOver = isGameOver,
                    message = when {
                        isWon -> UiMessage(R.string.msg_win)
                        isGameOver -> UiMessage(R.string.msg_game_over, listOf(state.targetWord.uppercase()))
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
        val allWords = wordRepository.getAllWords(currentLanguage).first()
        val possible = allWords.filter { engine.isPossibleWord(it.word, _uiState.value.guesses) }
        _uiState.update { it.copy(possibleWordsCount = possible.size) }
    }

    fun onHintRequest() {
        val state = _uiState.value
        if (state.isGameOver) return

        viewModelScope.launch {
            // 1. If the user has not yet entered a word, suggest a word to try
            if (state.guesses.isEmpty()) {
                val randomWord = wordRepository.getRandomWord(currentLanguage)?.word?.uppercase() ?: "APPLE"
                _uiState.update { it.copy(message = UiMessage(R.string.msg_try_word, listOf(randomWord))) }
                return@launch
            }

            // Identify known correct positions (green letters)
            val knownCorrect = MutableList(5) { ' ' }
            state.guesses.forEach { guess ->
                guess.forEachIndexed { index, evaluated ->
                    if (evaluated.status == LetterStatus.CORRECT) {
                        knownCorrect[index] = evaluated.char.uppercaseChar()
                    }
                }
            }

            // 2 & 3. Find letters that are in the word but incorrectly placed (yellow letters)
            // We look for letters marked PRESENT anywhere on the board.
            val yellowLetters = mutableSetOf<Char>()
            state.guesses.forEach { guess ->
                guess.forEach { evaluated ->
                    if (evaluated.status == LetterStatus.PRESENT) {
                        yellowLetters.add(evaluated.char.uppercaseChar())
                    }
                }
            }

            val target = state.targetWord.uppercase()
            
            // Check if any of these "yellow" letters have positions in the target that are not yet solved (green)
            for (letter in yellowLetters.sorted()) { // Sort for deterministic reveal order
                val unsolvedIndices = target.mapIndexedNotNull { index, c ->
                    if (c == letter && knownCorrect[index] == ' ') index else null
                }
                
                if (unsolvedIndices.isNotEmpty()) {
                    // Reveal where to place one of the incorrect placed letters
                    val revealIndex = unsolvedIndices.first()
                    _uiState.update { it.copy(message = UiMessage(R.string.msg_letter_hint, listOf(letter, revealIndex + 1))) }
                    return@launch
                }
            }

            // 4. If there are no more incorrect placed letters (all yellow/green info is accounted for),
            // then show a list of available words that match ALL information on the board (correct, present, and absent).
            val allWords = wordRepository.getAllWords(currentLanguage).first()
            val matching = allWords.filter { word ->
                engine.isPossibleWord(word.word, state.guesses)
            }.map { it.word.uppercase() }

            _uiState.update { 
                it.copy(
                    matchingWords = matching,
                    showPossibleWordsDialog = true
                )
            }
        }
    }

    fun dismissPossibleWordsDialog() {
        _uiState.update { it.copy(showPossibleWordsDialog = false) }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun dismissAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun showSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun dismissSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun onLanguageSelected(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
            _uiState.update { it.copy(showSettingsDialog = false) }
            // Note: The UI will need to react to this change. 
            // In a real app, you might use AppCompatDelegate.setApplicationLocales
        }
    }

    fun onWordsImported(words: List<String>) {
        if (words.isEmpty()) {
            _uiState.update { it.copy(message = UiMessage(R.string.msg_import_empty)) }
            return
        }
        _uiState.update { it.copy(pendingImportWords = words, showImportOptionsDialog = true) }
    }

    fun confirmImport(replace: Boolean) {
        val words = _uiState.value.pendingImportWords
        viewModelScope.launch {
            if (replace) {
                wordRepository.deleteWordsByLanguage(currentLanguage)
            }
            val wordEntities = words.map { Word(it.lowercase(), currentLanguage) }
            wordRepository.insertAll(wordEntities)
            _uiState.update { 
                it.copy(
                    showImportOptionsDialog = false,
                    pendingImportWords = emptyList(),
                    message = UiMessage(R.string.msg_import_success, listOf(words.size))
                )
            }
            updatePossibleWordsCount()
        }
    }

    fun dismissImportOptionsDialog() {
        _uiState.update { it.copy(showImportOptionsDialog = false, pendingImportWords = emptyList()) }
    }

    private fun saveGameState() {
        viewModelScope.launch {
            val state = _uiState.value
            gameStateRepository.saveGameState(
                GameState(
                    targetWord = state.targetWord,
                    guesses = state.guesses.map { evaluatedList -> evaluatedList.map { it.char }.joinToString("") },
                    isGameOver = state.isGameOver,
                    isWon = state.isWon,
                    language = currentLanguage
                )
            )
        }
    }
}
