package dk.rpix.wordle.data

import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()
    suspend fun getWordCount(): Int = wordDao.getWordCount()
    suspend fun insertAll(words: List<Word>) = wordDao.insertAll(words)
    suspend fun getRandomWord(): Word? = wordDao.getRandomWord()
    suspend fun isWordValid(word: String): Boolean = wordDao.isWordValid(word)
}
