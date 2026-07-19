package dk.rpix.wordle.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.rpix.wordle.game.GameViewModel
import dk.rpix.wordle.ui.components.WordleGrid
import dk.rpix.wordle.ui.components.WordleKeyboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLargeScreen = adaptiveInfo.windowSizeClass.windowWidthSizeClass.toString() != "Compact"

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "WORDLE",
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
                        Icon(Icons.Rounded.Refresh, contentDescription = "New Game")
                    }
                    IconButton(onClick = { viewModel.requestHint() }) {
                        Icon(Icons.Rounded.Help, contentDescription = "Word Hint")
                    }
                    IconButton(onClick = { viewModel.revealLetterHint() }) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = "Letter Hint")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLargeScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    WordleGrid(
                        guesses = uiState.guesses,
                        currentGuess = uiState.currentGuess
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    WordleKeyboard(
                        keyboardState = uiState.keyboardState,
                        onLetterInput = viewModel::onLetterInput,
                        onDelete = viewModel::onDelete,
                        onSubmit = viewModel::onSubmit
                    )
                }
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
                    modifier = Modifier.weight(1f)
                )
                WordleKeyboard(
                    keyboardState = uiState.keyboardState,
                    onLetterInput = viewModel::onLetterInput,
                    onDelete = viewModel::onDelete,
                    onSubmit = viewModel::onSubmit
                )
            }
        }
    }
}
