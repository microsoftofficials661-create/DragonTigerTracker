package com.example.dragontracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppBlack = Color(0xFF101014)
private val CardBlack = Color(0xFF1D1D24)
private val DarkButton = Color(0xFF16161B)
private val AccentRed = Color(0xFFE53935)
private val AccentGold = Color(0xFFFFC857)
private val SoftWhite = Color(0xFFF4F4F7)
private val ProfitGreen = Color(0xFF4CAF50)
private val CircleDotRed = Color(0xFFEF5350)
private val CircleDotBlack = Color(0xFF424242)

// 8-Level betting sequence according to your trick sheet
val BET_LEVELS = listOf(10, 20, 50, 100, 200, 400, 800, 1650)

data class GameEntry(
    val id: Long,
    val first: String,
    val second: String,
    val suggestion: String,
    val actual: String,
    val betChoice: String,
    val level: Int,
    val stake: Int,
    val profitLoss: Double,
    val timestamp: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AccentRed,
                    secondary = AccentGold,
                    background = AppBlack,
                    surface = CardBlack,
                    onPrimary = Color.White,
                    onBackground = SoftWhite,
                    onSurface = SoftWhite
                )
            ) {
                TrackerApp(
                    loadEntries = { loadSavedEntries() },
                    saveEntries = { saveSavedEntries(it) }
                )
            }
        }
    }

    private fun loadSavedEntries(): List<GameEntry> {
        val prefs = getSharedPreferences("DragonTigerPrefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("entries", null) ?: return emptyList()
        val list = mutableListOf<GameEntry>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GameEntry(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        first = obj.optString("first", "Red"),
                        second = obj.optString("second", "Red"),
                        suggestion = obj.optString("suggestion", "Dragon"),
                        actual = obj.optString("actual", "Dragon"),
                        betChoice = obj.optString("betChoice", "Dragon"),
                        level = obj.optInt("level", 1),
                        stake = obj.optInt("stake", 10),
                        profitLoss = obj.optDouble("profitLoss", 0.0),
                        timestamp = obj.optString("timestamp", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveSavedEntries(entries: List<GameEntry>) {
        val prefs = getSharedPreferences("DragonTigerPrefs", Context.MODE_PRIVATE)
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("first", entry.first)
            obj.put("second", entry.second)
            obj.put("suggestion", entry.suggestion)
            obj.put("actual", entry.actual)
            obj.put("betChoice", entry.betChoice)
            obj.put("level", entry.level)
            obj.put("stake", entry.stake)
            obj.put("profitLoss", entry.profitLoss)
            obj.put("timestamp", entry.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("entries", array.toString()).apply()
    }
}

@Composable
private fun TrackerApp(
    loadEntries: () -> List<GameEntry>,
    saveEntries: (List<GameEntry>) -> Unit
) {
    var first by remember { mutableStateOf("Red") }
    var second by remember { mutableStateOf("Red") }
    var actual by remember { mutableStateOf("Dragon") }
    var currentLevel by remember { mutableIntStateOf(1) }
    var entries by remember { mutableStateOf(loadEntries()) }
    var tab by remember { mutableStateOf("Tracker") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Trick Sheet Rule: Black + Black -> Tiger, else Dragon
    val suggestion = if (first == "Black" && second == "Black") "Tiger" else "Dragon"
    val currentStake = BET_LEVELS.getOrElse(currentLevel - 1) { BET_LEVELS.last() }

    val totalPnL = entries.sumOf { it.profitLoss }
    val dragonCount = entries.count { it.actual == "Dragon" }
    val tigerCount = entries.count { it.actual == "Tiger" }
    val tieCount = entries.count { it.actual == "Tie" }

    Scaffold(
        containerColor = AppBlack,
        bottomBar = {
            NavigationBar(containerColor = CardBlack, contentColor = SoftWhite) {
                listOf("Tracker", "History", "Stats").forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = {
                            Text(
                                when (item) {
                                    "Tracker" -> "🎯"
                                    "History" -> "📜"
                                    else -> "📊"
                                },
                                fontSize = 18.sp
                            )
                        },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            selectedTextColor = AccentGold,
                            indicatorColor = AccentRed.copy(alpha = 0.35f),
                            unselectedTextColor = SoftWhite.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBlack)
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("WELCOME BACK,", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("MOHAN", color = SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        "PRO EDITION",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("DRAGON TIGER", color = AccentRed, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("Colour Card Tracker", color = SoftWhite.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            when (tab) {
                "Tracker" -> {
                    // Vertical scroll wrapper so nothing is cut off in mini-window mode
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Level: $currentLevel of ${BET_LEVELS.size}", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Next Bet: ₹$currentStake", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total P&L", color = SoftWhite.copy(alpha = 0.6f), fontSize = 11.sp)
                                    val pnlColor = if (totalPnL >= 0) ProfitGreen else AccentRed
                                    Text(
                                        "${if (totalPnL >= 0) "+" else ""}${String.format(Locale.US, "%.2f", totalPnL)}",
                                        color = pnlColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text("Select colours", color = SoftWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))

                        Text("FIRST COLOUR", color = SoftWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColourPillButton(
                                title = "RED",
                                dotColor = CircleDotRed,
                                isSelected = first == "Red",
                                modifier = Modifier.weight(1f)
                            ) { first = "Red" }
                            ColourPillButton(
                                title = "BLACK",
                                dotColor = CircleDotBlack,
                                isSelected = first == "Black",
                                modifier = Modifier.weight(1f)
                            ) { first = "Black" }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("SECOND COLOUR", color = SoftWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColourPillButton(
                                title = "RED",
                                dotColor = CircleDotRed,
                                isSelected = second == "Red",
                                modifier = Modifier.weight(1f)
                            ) { second = "Red" }
                            ColourPillButton(
                                title = "BLACK",
                                dotColor = CircleDotBlack,
                                isSelected = second == "Black",
                                modifier = Modifier.weight(1f)
                            ) { second = "Black" }
                        }

                        Spacer(Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("RULE-BASED SUGGESTION", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Text(suggestion.uppercase(Locale.ROOT), color = AccentRed, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text("ACTUAL RESULT", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ResultPillButton(title = "Dragon", isSelected = actual == "Dragon", modifier = Modifier.weight(1f)) {
                                actual = "Dragon"
                            }
                            ResultPillButton(title = "Tiger", isSelected = actual == "Tiger", modifier = Modifier.weight(1f)) {
                                actual = "Tiger"
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val stamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                                val betChoice = suggestion
                                val isWin = betChoice == actual

                                val pnl = when {
                                    actual == "Tie" -> -(currentStake * 0.50)
                                    isWin -> currentStake * 0.95
                                    else -> -(currentStake.toDouble())
                                }

                                val entry = GameEntry(
                                    id = System.currentTimeMillis(),
                                    first = first,
                                    second = second,
                                    suggestion = suggestion,
                                    actual = actual,
                                    betChoice = betChoice,
                                    level = currentLevel,
                                    stake = currentStake,
                                    profitLoss = pnl,
                                    timestamp = stamp
                                )

                                entries = listOf(entry) + entries
                                saveEntries(entries)

                                if (isWin) {
                                    currentLevel = 1
                                } else if (actual != "Tie") {
                                    currentLevel = if (currentLevel < BET_LEVELS.size) currentLevel + 1 else 1
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text("SAVE ENTRY", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }

                "History" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Game history", color = SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showClearDialog = true }) {
                            Text("Clear all", color = AccentRed)
                        }
                    }

                    if (entries.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No entries saved yet. Add a colour entry from the Tracker tab.", color = SoftWhite.copy(alpha = 0.6f))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(entries, key = { it.id }) { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardBlack),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Lvl ${entry.level} | ₹${entry.stake} on ${entry.betChoice}", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Cards: ${entry.first}+${entry.second} → ${entry.actual}", color = SoftWhite.copy(alpha = 0.65f), fontSize = 11.sp)
                                        }
                                        val win = entry.profitLoss > 0
                                        Text(
                                            "${if (win) "+" else ""}${String.format(Locale.US, "%.2f", entry.profitLoss)}",
                                            color = if (win) ProfitGreen else AccentRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text("Recorded statistics", color = SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    StatCard("Player", "Mohan")
                    Spacer(Modifier.height(6.dp))
                    StatCard("Total entries", "${entries.size}")
                    Spacer(Modifier.height(6.dp))
                    StatCard("Total P&L", "${if (totalPnL >= 0) "+" else ""}${String.format(Locale.US, "%.2f", totalPnL)}")
                    Spacer(Modifier.height(6.dp))
                    StatCard("Actual Dragon results", "$dragonCount")
                    Spacer(Modifier.height(6.dp))
                    StatCard("Actual Tiger results", "$tigerCount")
                    Spacer(Modifier.height(6.dp))
                    StatCard("Tie results", "$tieCount")
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will reset all your tracked entries.") },
            confirmButton = {
                TextButton(onClick = {
                    entries = emptyList()
                    saveEntries(entries)
                    currentLevel = 1
                    showClearDialog = false
                }) {
                    Text("Clear", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = SoftWhite)
                }
            }
        )
    }
}

@Composable
private fun ColourPillButton(
    title: String,
    dotColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkButton,
        modifier = modifier
            .height(40.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentRed else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, shape = CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(title, color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ResultPillButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkButton,
        modifier = modifier
            .height(38.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentRed else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                title,
                color = if (isSelected) SoftWhite else SoftWhite.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = SoftWhite, fontSize = 13.sp)
            Text(value, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
