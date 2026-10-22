package com.example.dttracker.data.local

import androidx.room.*
import com.example.dttracker.data.model.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Query("DELETE FROM rounds_history WHERE id = (SELECT MAX(id) FROM rounds_history)")
    suspend fun deleteLatestRound()

    @Query("SELECT * FROM rounds_history ORDER BY timestamp DESC")
    fun getAllRounds(): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRound(): RoundEntity?

    @Query("SELECT COALESCE(SUM(pnlDelta), 0.0) FROM rounds_history WHERE dateString = :date")
    fun getDailyPnl(date: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(betAmount), 0.0) FROM rounds_history WHERE dateString = :date")
    fun getDailyTurnover(date: String): Flow<Double>
}
