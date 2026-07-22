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
                
                val words = loadWords(localizedContext, R.raw.words, language, false)
                val rareWords = loadWords(localizedContext, R.raw.rare, language, true)
                
                // Combine lists, preferring common status if a word exists in both
                val combinedWords = (words + rareWords)
                    .groupBy { it.word }
                    .map { (_, versions) -> 
                        versions.find { !it.isRare } ?: versions.first()
                    }
                
                repository.insertAll(combinedWords)
            }
        }
    }

    private fun loadWords(context: Context, resourceId: Int, language: String, isRare: Boolean): List<Word> {
        return try {
            context.resources.openRawResource(resourceId)
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .map { it.trim().lowercase() }
                .filter { it.length == 5 }
                .distinct()
                .map { Word(it, language, isRare) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
