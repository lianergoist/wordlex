package dk.rpix.wordle.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.rpix.wordle.R
import dk.rpix.wordle.game.GameViewModel
import dk.rpix.wordle.ui.asString
import dk.rpix.wordle.ui.components.WordleGrid
import dk.rpix.wordle.ui.components.WordleKeyboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    // Use Row layout only if in Landscape
    val isWideLayout = isLandscape
    
    // Dynamic sizing for Portrait (Stacked)
    val isSmallHeight = configuration.screenHeightDp < 650
    val isSmallWidth = configuration.screenWidthDp < 380
    // More inclusive tablet detection (e.g. Medium tablets)
    val isTabletPortrait = configuration.screenWidthDp >= 600 && !isWideLayout
    
    val portraitCellSize = when {
        isTabletPortrait -> 72.dp // Larger for tablets
        isSmallHeight || isSmallWidth -> 42.dp
        else -> 48.dp
    }
    val portraitKeyHeight = when {
        isTabletPortrait -> 84.dp // Larger for tablets
        isSmallHeight || isSmallWidth -> 46.dp
        else -> 54.dp
    }
    val portraitSpacing = if (isSmallHeight || isSmallWidth) 4.dp else 8.dp
    
    // Dynamic sizing for Landscape/Wide (Side-by-side)
    val isTabletWide = configuration.screenWidthDp >= 840 // Material Expanded breakpoint
    val isCompactHeight = configuration.screenHeightDp < 480
    
    val landscapeCellSize = when {
        isCompactHeight -> 44.dp // Priority 1: Fit phone height
        isTabletWide -> 64.dp    // Priority 2: Use large size for wide tablets
        else -> 38.dp
    }
    val landscapeKeyHeight = when {
        isCompactHeight -> 48.dp
        isTabletWide -> 72.dp
        else -> 42.dp
    }
    val landscapeKeyWidth = when {
        isCompactHeight -> 32.dp
        isTabletWide -> 52.dp
        else -> 26.dp
    }
    val localizedLandscapeKeyWidth = when {
        isCompactHeight -> 28.dp
        isTabletWide -> 42.dp
        else -> 22.dp
    }
    
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

    val currentMessage = uiState.message?.asString()

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
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text("${uiState.points}")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onNewGameClick() }) {
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
                                text = { Text(stringResource(R.string.menu_points)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.showPointsDialog()
                                }
                            )
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
        snackbarHost = { }
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
                        val currentLangLabel = when (language) {
                            "da" -> stringResource(R.string.lang_da)
                            "de" -> stringResource(R.string.lang_de)
                            "fr" -> stringResource(R.string.lang_fr)
                            "es" -> stringResource(R.string.lang_es)
                            else -> stringResource(R.string.lang_en)
                        }
                        
                        SettingsItem(
                            headline = stringResource(R.string.settings_language),
                            subtext = currentLangLabel,
                            onClick = {
                                viewModel.dismissSettingsDialog()
                                viewModel.showLanguageSelectionDialog()
                            }
                        )
                        
                        SettingsItem(
                            headline = stringResource(R.string.settings_import_words),
                            subtext = "",
                            onClick = {
                                viewModel.dismissSettingsDialog()
                                viewModel.showImportSettingsDialog()
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissSettingsDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showLanguageSelectionDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissLanguageSelectionDialog,
                title = { Text(stringResource(R.string.settings_language)) },
                text = {
                    Column {
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
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = language == code,
                                    onClick = { viewModel.onLanguageSelected(code) },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissLanguageSelectionDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showImportSettingsDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissImportSettingsDialog,
                title = { Text(stringResource(R.string.import_dialog_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge)
                        val languages = listOf(
                            "en" to stringResource(R.string.lang_en),
                            "da" to stringResource(R.string.lang_da),
                            "de" to stringResource(R.string.lang_de),
                            "fr" to stringResource(R.string.lang_fr),
                            "es" to stringResource(R.string.lang_es)
                        )
                        
                        // Language Selection for Import
                        Column {
                            languages.forEach { (code, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onImportLanguageChanged(code) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.importSettingsLanguage == code,
                                        onClick = { viewModel.onImportLanguageChanged(code) },
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        HorizontalDivider()
                        
                        // Add/Replace Selection
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onImportReplaceChanged(false) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !uiState.importSettingsReplace,
                                    onClick = { viewModel.onImportReplaceChanged(false) },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_add), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onImportReplaceChanged(true) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.importSettingsReplace,
                                    onClick = { viewModel.onImportReplaceChanged(true) },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_replace), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { filePickerLauncher.launch("text/plain") }) {
                        Text(stringResource(R.string.settings_import_words))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissImportSettingsDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (uiState.showNewGameConfirmDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissNewGameConfirmDialog,
                title = { Text(stringResource(R.string.confirm_new_game_title)) },
                text = { Text(stringResource(R.string.confirm_new_game_text)) },
                confirmButton = {
                    Button(onClick = { viewModel.startNewGame() }) {
                        Text(stringResource(R.string.btn_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissNewGameConfirmDialog) {
                        Text(stringResource(R.string.btn_no))
                    }
                }
            )
        }

        if (uiState.showHintConfirmDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissHintConfirmDialog,
                title = { Text(stringResource(R.string.hint_confirm_title)) },
                text = { Text(stringResource(R.string.hint_confirm_text, uiState.pendingHintPrice)) },
                confirmButton = {
                    Button(onClick = viewModel::confirmHint) {
                        Text(stringResource(R.string.btn_buy_hint))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissHintConfirmDialog) {
                        Text(stringResource(R.string.btn_no))
                    }
                }
            )
        }

        if (uiState.showPointsDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissPointsDialog,
                title = { Text(stringResource(R.string.stats_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatsRow(stringResource(R.string.stats_games_played), "${uiState.statistics?.totalGames ?: 0}")
                        StatsRow(stringResource(R.string.stats_total_points), "${uiState.statistics?.totalPoints ?: 0}")
                        StatsRow(stringResource(R.string.stats_average_points), "%.1f".format(uiState.statistics?.averagePoints ?: 0f))
                    }
                },
                confirmButton = {
                    Button(onClick = viewModel::dismissPointsDialog) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        if (isWideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WordleGrid(
                    guesses = uiState.guesses,
                    currentGuess = uiState.currentGuess,
                    focusedIndex = uiState.focusedIndex,
                    onCellClick = viewModel::onCellClick,
                    cellSize = landscapeCellSize,
                    spacing = 4.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameMessageDisplay(currentMessage)
                    
                    WordleKeyboard(
                        keyboardState = uiState.keyboardState,
                        onLetterInput = viewModel::onLetterInput,
                        onDelete = viewModel::onDelete,
                        onSubmit = viewModel::onSubmit,
                        keyWidth = if (language == "da" || language == "de" || language == "es") localizedLandscapeKeyWidth else landscapeKeyWidth,
                        keyHeight = landscapeKeyHeight,
                        spacing = 4.dp,
                        rows = keyboardRows
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(8.dp))
                WordleGrid(
                    guesses = uiState.guesses,
                    currentGuess = uiState.currentGuess,
                    focusedIndex = uiState.focusedIndex,
                    onCellClick = viewModel::onCellClick,
                    cellSize = portraitCellSize,
                    spacing = portraitSpacing,
                    modifier = Modifier.weight(1f)
                )
                
                GameMessageDisplay(currentMessage)
                
                WordleKeyboard(
                    keyboardState = uiState.keyboardState,
                    onLetterInput = viewModel::onLetterInput,
                    onDelete = viewModel::onDelete,
                    onSubmit = viewModel::onSubmit,
                    keyWidth = if (language == "da" || language == "de" || language == "es" || language == "fr") 
                        (if (isTabletPortrait) 54.dp else 26.dp) 
                    else 
                        (if (isTabletPortrait) 64.dp else 30.dp),
                    keyHeight = portraitKeyHeight,
                    spacing = if (language == "da" || language == "de" || language == "es" || language == "fr") 4.dp else 6.dp,
                    rows = keyboardRows
                )
            }
        }
    }
}

@Composable
fun GameMessageDisplay(message: String?) {
    Box(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (message != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsItem(
    headline: String,
    subtext: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.titleLarge
        )
        if (subtext.isNotEmpty()) {
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
