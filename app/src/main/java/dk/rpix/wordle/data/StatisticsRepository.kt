package dk.rpix.wordle.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statisticsDataStore by preferencesDataStore(name = "statistics")

data class Statistics(
    val totalGames: Int,
    val totalPoints: Int
) {
    val averagePoints: Float
        get() = if (totalGames > 0) totalPoints.toFloat() / totalGames else 0f
}

class StatisticsRepository(private val context: Context) {
    private val totalGamesKey = intPreferencesKey("total_games")
    private val totalPointsKey = intPreferencesKey("total_points")

    val statisticsFlow: Flow<Statistics> = context.statisticsDataStore.data.map { preferences ->
        Statistics(
            totalGames = preferences[totalGamesKey] ?: 0,
            totalPoints = preferences[totalPointsKey] ?: 0
        )
    }

    suspend fun addGameResult(points: Int) {
        context.statisticsDataStore.edit { preferences ->
            val currentGames = preferences[totalGamesKey] ?: 0
            val currentPoints = preferences[totalPointsKey] ?: 0
            preferences[totalGamesKey] = currentGames + 1
            preferences[totalPointsKey] = currentPoints + points
        }
    }
}
