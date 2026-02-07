package cz.kocika.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.kocika.game.CatApplication
import cz.kocika.game.ui.screens.MainScreen
import cz.kocika.game.ui.theme.AIKocikaTheme
import cz.kocika.game.ui.viewmodel.CatViewModel
import cz.kocika.game.ui.viewmodel.CatViewModelFactory
import cz.kocika.game.utils.TtsManager

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsManager = TtsManager(this)

        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as CatApplication
            val viewModel: CatViewModel = viewModel(
                factory = CatViewModelFactory(app.repository)
            )

            val storyText by viewModel.storyText.collectAsState()
            LaunchedEffect(storyText) {
                storyText?.let { ttsManager.speak(it) }
            }
            
            AIKocikaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
