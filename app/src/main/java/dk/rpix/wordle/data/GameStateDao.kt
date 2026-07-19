package dk.rpix.wordle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 0")
    suspend fun getGameState(): GameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(gameState: GameState)

    @Query("DELETE FROM game_state")
    suspend fun clearGameState()
}
