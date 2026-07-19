package dk.rpix.wordle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 0,
    val targetWord: String,
    val guesses: List<String>,
    val isGameOver: Boolean,
    val isWon: Boolean
)
