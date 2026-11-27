package com.example.dragontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val AccentRed = Color(0xFFE53935)
private val AccentGold = Color(0xFFFFC857)
private val SoftWhite = Color(0xFFF4F4F7)

data class GameEntry(
    val id: Long,
    val first: String,
    val second: String,
    val suggestion: String,
    val actual: String,
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
        val raw = getSharedPreferences("tracker", MODE_PRIVATE)
            .getString("entries", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                GameEntry(
                    id = o.optLong("id"),
                    first = o.optString("first"),
                    second = o.optString("second"),
                    suggestion = o.optString("suggestion"),
                    actual = o.optString("actual"),
                    timestamp = o.optString("timestamp")
                )
            }.reversed()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveSavedEntries(entries: List<GameEntry>) {
        val array = JSONArray()
        entries.reversed().forEach { e ->
            array.put(JSONObject().apply {
                put("id", e.id)
                put("first", e.first)
                put("second", e.second)
                put("suggestion", e.suggestion)
                put("actual", e.actual)
                put("timestamp", e.timestamp)
            })
        }
        getSharedPreferences("tracker", MODE_PRIVATE)
            .edit().putString("entries", array.toString()).apply()
    }
}

@Composable
private fun TrackerApp(
    loadEntries: () -> List<GameEntry>,
    saveEntries: (List<GameEntry>) -> Unit
) {
    var first by remember { mutableStateOf("Red") }
    var second by remember { mutableStateOf("Red") }
    var actual by remember { mutableStateOf("Not entered") }
    var entries by remember { mutableStateOf(loadEntries()) }
    var tab by remember { mutableStateOf("Tracker") }
    var showClearDialog by remember { mutableStateOf(false) }

    val suggestion = when {
        first == "Black" && second == "Black" -> "Tiger"
        else -> "Dragon"
    }
    val dragonCount = entries.count { it.actual == "Dragon" }
    val tigerCount = entries.count { it.actual == "Tiger" }
    val savedCount = entries.size

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
                                    "Tracker" -> "🎴"
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
                            indicatorColor = AccentRed.copy(alpha = 0.25f),
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
                .padding(16.dp)
        ) {
            Text("DRAGON TIGER", color = AccentRed, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("Colour Card Tracker", color = SoftWhite.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            when (tab) {
                "Tracker" -> {
                    Text("Select colours", color = SoftWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    ColourPicker("FIRST COLOUR", first) { first = it }
                    Spacer(Modifier.height(10.dp))
                    ColourPicker("SECOND COLOUR", second) { second = it }
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        color = CardBlack,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("RULE-BASED SUGGESTION", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(suggestion.uppercase(), color = if (suggestion == "Dragon") AccentRed else AccentGold,
                                fontSize = 30.sp, fontWeight = FontWeight.Black)
                            Text(
                                "This is only your saved colour rule, not a reliable prediction of the actual game result.",
                                color = SoftWhite.copy(alpha = 0.65f), fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("ACTUAL RESULT (optional)", color = SoftWhite, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ChoiceButton("Dragon", actual == "Dragon", Modifier.weight(1f)) { actual = "Dragon" }
                        ChoiceButton("Tiger", actual == "Tiger", Modifier.weight(1f)) { actual = "Tiger" }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { actual = "Not entered" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear actual result") }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val stamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                            val entry = GameEntry(
                                id = System.currentTimeMillis(),
                                first = first,
                                second = second,
                                suggestion = suggestion,
                                actual = actual,
                                timestamp = stamp
                            )
                            entries = listOf(entry) + entries
                            saveEntries(entries)
                            actual = "Not entered"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) { Text("SAVE ENTRY", fontWeight = FontWeight.Bold) }
                }

                "History" -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Game history", color = SoftWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = { showClearDialog = true }) { Text("Clear all", color = AccentRed) }
                    }
                    if (entries.isEmpty()) {
                        EmptyMessage("No entries saved yet. Add a colour entry from the Tracker tab.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(entries, key = { it.id }) { entry ->
                                HistoryCard(entry = entry, onDelete = {
                                    entries = entries.filterNot { it.id == entry.id }
                                    saveEntries(entries)
                                })
                            }
                        }
                    }
                }

                else -> {
                    Text("Recorded statistics", color = SoftWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    StatCard("Total entries", savedCount.toString())
                    Spacer(Modifier.height(8.dp))
                    StatCard("Actual Dragon results", dragonCount.toString())
                    Spacer(Modifier.height(8.dp))
                    StatCard("Actual Tiger results", tigerCount.toString())
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Statistics summarize only the actual results you entered. They do not predict future results.",
                        color = SoftWhite.copy(alpha = 0.65f), fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This permanently removes saved entries from this app.") },
            confirmButton = {
                TextButton(onClick = {
                    entries = emptyList()
                    saveEntries(entries)
                    showClearDialog = false
                }) { Text("Clear", color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
            containerColor = CardBlack,
            titleContentColor = SoftWhite,
            textContentColor = SoftWhite
        )
    }
}

@Composable
private fun ColourPicker(title: String, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(title, color = SoftWhite.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ChoiceButton("🔴  RED", selected == "Red", Modifier.weight(1f)) { onSelect("Red") }
            ChoiceButton("⚫  BLACK", selected == "Black", Modifier.weight(1f)) { onSelect("Black") }
        }
    }
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) AccentRed.copy(alpha = 0.22f) else CardBlack,
            contentColor = SoftWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) AccentRed else SoftWhite.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun HistoryCard(entry: GameEntry, onDelete: () -> Unit) {
    Surface(color = CardBlack, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${entry.id.toString().takeLast(5)}", color = AccentGold, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text(entry.timestamp, color = SoftWhite.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text("${entry.first} + ${entry.second}", color = SoftWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Rule suggestion: ${entry.suggestion}", color = AccentGold, fontSize = 13.sp)
            Text("Actual result: ${entry.actual}", color = SoftWhite.copy(alpha = 0.8f), fontSize = 13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) { Text("Delete entry", color = AccentRed) }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Surface(color = CardBlack, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = SoftWhite.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            Text(value, color = AccentGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun EmptyMessage(message: String) {
    Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
        Text(message, color = SoftWhite.copy(alpha = 0.65f))
    }
}
