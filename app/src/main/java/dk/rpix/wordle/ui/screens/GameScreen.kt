package dk.rpix.wordle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import dk.rpix.wordle.R
import dk.rpix.wordle.game.GameViewModel
import dk.rpix.wordle.ui.asString
import dk.rpix.wordle.ui.components.WordleGrid
import dk.rpix.wordle.ui.components.WordleKeyboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.rounded.MoreVert
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLandscape = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val words = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                reader.readLines()
                    .map { line -> line.trim() }
                    .filter { word -> word.length == 5 && word.all { c -> c.isLetter() } }
            } ?: emptyList()
            viewModel.onWordsImported(words)
        }
    }

    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language

    val keyboardRows = when (language) {
        "da" -> listOf(
            "QWERTYUIOPÅ".toList(),
            "ASDFGHJKLÆØ".toList(),
            "ZXCVBNM".toList()
        )
        "de" -> listOf(
            "QWERTZUIOPÜ".toList(),
            "ASDFGHJKLÖÄ".toList(),
            "YXCVBNM".toList()
        )
        "fr" -> listOf(
            "AZERTYUIOP".toList(),
            "QSDFGHJKLM".toList(),
            "WXCVBN".toList()
        )
        "es" -> listOf(
            "QWERTYUIOP".toList(),
            "ASDFGHJKLÑ".toList(),
            "ZXCVBNM".toList()
        )
        else -> listOf(
            "QWERTYUIOP".toList(),
            "ASDFGHJKL".toList(),
            "ZXCVBNM".toList()
        )
    }

    val isLocalized = language in listOf("da", "de", "fr", "es")

    val currentMessage = uiState.message?.asString()

    LaunchedEffect(currentMessage) {
        currentMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.title_wordle),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text("${uiState.possibleWordsCount}")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewGame() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.btn_new_game))
                    }
                    IconButton(onClick = { viewModel.onHintRequest() }) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = stringResource(R.string.btn_hint))
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.showSettingsDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.showAboutDialog()
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.showPossibleWordsDialog) {
            dk.rpix.wordle.ui.components.HintDialog(
                possibleWords = uiState.matchingWords,
                onDismiss = viewModel::dismissPossibleWordsDialog
            )
        }

        if (uiState.showAboutDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissAboutDialog,
                title = { Text(stringResource(R.string.about_title)) },
                text = { Text(stringResource(R.string.about_text)) },
                confirmButton = {
                    Button(onClick = viewModel::dismissAboutDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showSettingsDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissSettingsDialog,
                title = { Text(stringResource(R.string.settings_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge)
                        val languages = listOf(
                            "en" to stringResource(R.string.lang_en),
                            "da" to stringResource(R.string.lang_da),
                            "de" to stringResource(R.string.lang_de),
                            "fr" to stringResource(R.string.lang_fr),
                            "es" to stringResource(R.string.lang_es)
                        )
                        languages.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onLanguageSelected(code) }
                                    .padding(vertical = 2.dp), // Even tighter
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = language == code,
                                    onClick = { viewModel.onLanguageSelected(code) },
                                    modifier = Modifier.size(32.dp) // Smaller radio button area
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Button(
                            onClick = { filePickerLauncher.launch("text/plain") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_import_words))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissSettingsDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showImportOptionsDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissImportOptionsDialog,
                title = { Text(stringResource(R.string.import_dialog_title)) },
                text = { Text(stringResource(R.string.import_dialog_text)) },
                confirmButton = {
                    Row {
                        TextButton(onClick = { viewModel.confirmImport(replace = false) }) {
                            Text(stringResource(R.string.btn_add))
                        }
                        TextButton(onClick = { viewModel.confirmImport(replace = true) }) {
                            Text(stringResource(R.string.btn_replace))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissImportOptionsDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showConfirmWordListDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissConfirmWordListDialog,
                title = { Text(stringResource(R.string.confirm_word_list_title)) },
                text = { Text(stringResource(R.string.confirm_word_list_text)) },
                confirmButton = {
                    Button(onClick = viewModel::showMatchingWords) {
                        Text(stringResource(R.string.btn_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissConfirmWordListDialog) {
                        Text(stringResource(R.string.btn_no))
                    }
                }
            )
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WordleGrid(
                    guesses = uiState.guesses,
                    currentGuess = uiState.currentGuess,
                    focusedIndex = uiState.focusedIndex,
                    onCellClick = viewModel::onCellClick,
                    cellSize = 38.dp,
                    spacing = 4.dp,
                    modifier = Modifier.padding(start = 32.dp)
                )
                Spacer(Modifier.width(20.dp))
                WordleKeyboard(
                    keyboardState = uiState.keyboardState,
                    onLetterInput = viewModel::onLetterInput,
                    onDelete = viewModel::onDelete,
                    onSubmit = viewModel::onSubmit,
                    keyWidth = if (language == "da" || language == "de") 22.dp else 28.dp, // Shrink keys if more letters
                    keyHeight = 42.dp,
                    spacing = 4.dp,
                    rows = keyboardRows,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(16.dp))
                WordleGrid(
                    guesses = uiState.guesses,
                    currentGuess = uiState.currentGuess,
                    focusedIndex = uiState.focusedIndex,
                    onCellClick = viewModel::onCellClick,
                    modifier = Modifier.weight(1f)
                )
                WordleKeyboard(
                    keyboardState = uiState.keyboardState,
                    onLetterInput = viewModel::onLetterInput,
                    onDelete = viewModel::onDelete,
                    onSubmit = viewModel::onSubmit,
                    rows = keyboardRows
                )
            }
        }
    }
}
