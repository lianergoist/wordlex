package dk.rpix.wordle.data

import android.content.Context
import android.content.res.Configuration
import dk.rpix.wordle.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class DataInitializer(private val context: Context, private val repository: WordRepository) {
    suspend fun populateDatabaseIfNeeded(language: String) {
        withContext(Dispatchers.IO) {
            if (repository.getWordCount(language) == 0) {
                val locale = Locale.forLanguageTag(language)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                val localizedContext = context.createConfigurationContext(config)
                
                val words = localizedContext.resources.openRawResource(R.raw.words)
                    .bufferedReader()
                    .readLines()
                    .filter { it.isNotBlank() }
                    .map { it.trim().lowercase() }
                    .filter { it.length == 5 }
                    .distinct()
                    .map { Word(it, language) }
                
                repository.insertAll(words)
            }
        }
    }
}
