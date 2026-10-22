package com.example.dttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds_history")
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val firstColor: CardColor,
    val secondColor: CardColor,
    val suggestedOutcome: GameChoice,
    val actualOutcome: GameResult,
    val levelApplied: Int,
    val betAmount: Double,
    val pnlDelta: Double,
    val runningPnl: Double
)
