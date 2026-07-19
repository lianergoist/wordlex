package dk.rpix.wordle.data

class GameStateRepository(private val gameStateDao: GameStateDao) {
    suspend fun getGameState(): GameState? = gameStateDao.getGameState()
    suspend fun saveGameState(gameState: GameState) = gameStateDao.saveGameState(gameState)
    suspend fun clearGameState() = gameStateDao.clearGameState()
}
