package dk.rpix.wordle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words", primaryKeys = ["word", "language"])
data class Word(
    val word: String,
    val language: String,
    val isRare: Boolean = false
)
