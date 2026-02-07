package cz.kocika.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordKeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kocika.game.ui.viewmodel.CatViewModel
import cz.kocika.game.model.AppSettings

@Composable
fun MainScreen(viewModel: CatViewModel = viewModel()) {
    val catState by viewModel.catState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var showParentalDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moje Kočička",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showParentalDialog = true }) {
                Text("⚙️", fontSize = 24.sp)
            }
        }

        // Cat Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCatEmoji(catState.mood),
                    fontSize = 80.sp
                )
            }
            
            val message by viewModel.lastMessage.collectAsState()
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Stats
        Column(modifier = Modifier.fillMaxWidth()) {
            StatBar("Hlad", catState.hunger, Color.Red)
            StatBar("Energie", catState.energy, Color.Yellow)
            StatBar("Hygiena", catState.hygiene, Color.Blue)
            StatBar("Nálada", catState.mood, Color.Green)
            StatBar("Zdraví", catState.health, Color.Magenta)
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Nakrmit", "🍲") { viewModel.feed() }
            ActionButton("Hrát", "🧶") { viewModel.play() }
            ActionButton("Uspat", "💤") { viewModel.sleep() }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Umýt", "🧼") { viewModel.wash() }
            ActionButton("Pohladit", "💖") { viewModel.pet() }
            ActionButton("Pohádka", "📖") { viewModel.generateStory() }
        }
    }

    // Story Dialog
    val storyText by viewModel.storyText.collectAsState()
    if (storyText != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearStory() },
            title = { Text("Pohádka") },
            text = { Text(storyText!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearStory() }) {
                    Text("Zavřít")
                }
            }
        )
    }

    if (showParentalDialog) {
        ParentalPinDialog(
            correctPin = settings.parentPin,
            onSuccess = {
                showParentalDialog = false
                showSettings = true
            },
            onDismiss = { showParentalDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            settings = settings,
            onSave = { 
                viewModel.updateSettings(it)
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp)
            Text(text = "$value/100", fontSize = 14.sp)
        }
        LinearProgressIndicator(
            progress = value / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ActionButton(label: String, icon: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = 100.dp, height = 80.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 24.sp)
            Text(text = label, fontSize = 10.sp)
        }
    }
}

fun getCatEmoji(mood: Int): String {
    return when {
        mood > 80 -> "😺"
        mood > 60 -> "🐱"
        mood > 40 -> "😿"
        else -> "😾"
    }
}

@Composable
fun ParentalPinDialog(correctPin: String, onSuccess: () -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rodičovský PIN") },
        text = {
            Column {
                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Zadejte PIN (výchozí 0000)") },
                    isError = error
                )
                if (error) {
                    Text("Nesprávný PIN", color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pin == correctPin) onSuccess() else error = true
            }) {
                Text("Vstoupit")
            }
        }
    )
}

@Composable
fun SettingsDialog(settings: AppSettings, onSave: (AppSettings) -> Unit, onDismiss: () -> Unit) {
    var aiEnabled by remember { mutableStateOf(settings.aiEnabled) }
    var storiesEnabled by remember { mutableStateOf(settings.storiesEnabled) }
    var playTimeLimit by remember { mutableStateOf(settings.playTimeLimitMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nastavení pro rodiče") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                    Text("Povolit AI odpovědi")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = storiesEnabled, onCheckedChange = { storiesEnabled = it })
                    Text("Povolit pohádky")
                }
                TextField(
                    value = playTimeLimit,
                    onValueChange = { playTimeLimit = it },
                    label = { Text("Časový limit (minuty)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(settings.copy(
                    aiEnabled = aiEnabled,
                    storiesEnabled = storiesEnabled,
                    playTimeLimitMinutes = playTimeLimit.toIntOrNull() ?: settings.playTimeLimitMinutes
                ))
            }) {
                Text("Uložit")
            }
        }
    )
}
