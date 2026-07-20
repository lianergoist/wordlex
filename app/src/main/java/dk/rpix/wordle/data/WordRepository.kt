package dk.rpix.wordle.data

import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {
    fun getAllWords(language: String): Flow<List<Word>> = wordDao.getAllWords(language)
    suspend fun getWordCount(language: String): Int = wordDao.getWordCount(language)
    suspend fun insertAll(words: List<Word>) = wordDao.insertAll(words)
    suspend fun getRandomWord(language: String): Word? = wordDao.getRandomWord(language)
    suspend fun isWordValid(word: String, language: String): Boolean = wordDao.isWordValid(word, language)
    suspend fun deleteWordsByLanguage(language: String) = wordDao.deleteWordsByLanguage(language)
}
