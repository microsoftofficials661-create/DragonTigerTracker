package com.example.dttracker.data.local

import androidx.room.TypeConverter
import com.example.dttracker.data.model.CardColor
import com.example.dttracker.data.model.GameChoice
import com.example.dttracker.data.model.GameResult

class Converters {
    @TypeConverter fun fromColor(v: CardColor) = v.name
    @TypeConverter fun toColor(v: String) = CardColor.valueOf(v)
    @TypeConverter fun fromChoice(v: GameChoice) = v.name
    @TypeConverter fun toChoice(v: String) = GameChoice.valueOf(v)
    @TypeConverter fun fromResult(v: GameResult) = v.name
    @TypeConverter fun toResult(v: String) = GameResult.valueOf(v)
}
