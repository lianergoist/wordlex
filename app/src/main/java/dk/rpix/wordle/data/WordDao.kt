package dk.rpix.wordle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE language = :language")
    fun getAllWords(language: String): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words WHERE language = :language")
    suspend fun getWordCount(language: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<Word>)

    @Query("SELECT * FROM words WHERE language = :language ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(language: String): Word?

    @Query("SELECT EXISTS(SELECT 1 FROM words WHERE word = :word AND language = :language LIMIT 1)")
    suspend fun isWordValid(word: String, language: String): Boolean

    @Query("DELETE FROM words WHERE language = :language")
    suspend fun deleteWordsByLanguage(language: String)
}
