package dk.rpix.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dk.rpix.wordle.R
import dk.rpix.wordle.data.DataInitializer
import dk.rpix.wordle.game.GameViewModel
import dk.rpix.wordle.game.GameViewModelFactory
import dk.rpix.wordle.navigation.GameRoute
import dk.rpix.wordle.ui.screens.GameScreen
import dk.rpix.wordle.ui.theme.WordleTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isInitialized by remember { mutableStateOf(false) }
            val app = application as WordleApplication

            val selectedLanguage by app.settingsRepository.languageFlow.collectAsState(initial = null)
            val currentLanguage = selectedLanguage ?: Locale.getDefault().language

            LaunchedEffect(currentLanguage) {
                DataInitializer(app, app.wordRepository).populateDatabaseIfNeeded(currentLanguage)
                isInitialized = true
            }

            val configuration = LocalConfiguration.current
            if (selectedLanguage != null) {
                val locale = Locale.forLanguageTag(selectedLanguage!!)
                Locale.setDefault(locale)
                configuration.setLocale(locale)
            }

            CompositionLocalProvider(LocalConfiguration provides configuration) {
                WordleTheme {
                    if (isInitialized) {
                        val backStack = remember { mutableStateListOf<Any>(GameRoute) }

                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = { key ->
                                when (key) {
                                    is GameRoute -> NavEntry(key) {
                                        val gameViewModel: GameViewModel = viewModel(
                                            factory = GameViewModelFactory(
                                                app.wordRepository, 
                                                app.gameStateRepository,
                                                app.settingsRepository,
                                                app.statisticsRepository
                                            )
                                        )
                                        GameScreen(viewModel = gameViewModel)
                                    }
                                    else -> NavEntry(Unit) { 
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(stringResource(R.string.unknown_route))
                                        }
                                    }
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
