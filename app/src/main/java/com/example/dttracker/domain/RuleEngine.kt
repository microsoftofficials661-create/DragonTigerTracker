package com.example.dttracker.domain

import com.example.dttracker.data.model.CardColor
import com.example.dttracker.data.model.GameChoice

object RuleEngine {
    val BET_LEVELS = listOf(10.0, 30.0, 90.0, 270.0, 810.0, 2430.0, 7290.0, 21870.0, 65610.0, 196830.0)

    fun getSuggestion(first: CardColor): GameChoice {
        return if (first == CardColor.RED) GameChoice.DRAGON else GameChoice.TIGER
    }
}
