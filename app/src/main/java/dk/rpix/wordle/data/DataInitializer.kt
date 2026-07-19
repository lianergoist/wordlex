package dk.rpix.wordle.data

import android.content.Context
import dk.rpix.wordle.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataInitializer(private val context: Context, private val repository: WordRepository) {
    suspend fun populateDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            if (repository.getWordCount() == 0) {
                val words = context.resources.openRawResource(R.raw.words)
                    .bufferedReader()
                    .readLines()
                    .filter { it.isNotBlank() }
                    .map { it.trim().lowercase() }
                    .filter { it.length == 5 }
                    .distinct()
                    .map { Word(it) }
                repository.insertAll(words)
            }
        }
    }
}
