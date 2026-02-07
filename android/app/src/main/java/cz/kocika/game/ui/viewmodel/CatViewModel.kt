package cz.kocika.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cz.kocika.game.data.api.CatStatusRequest
import cz.kocika.game.data.api.RetrofitClient
import cz.kocika.game.data.repository.CatRepository
import cz.kocika.game.model.AppSettings
import cz.kocika.game.model.CatState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatViewModel(private val repository: CatRepository) : ViewModel() {
    
    val catState: StateFlow<CatState> = repository.catState
        .map { it ?: CatState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CatState()
        )

    val settings: StateFlow<AppSettings> = repository.settings
        .map { it ?: AppSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _lastMessage = MutableStateFlow("Vítejte!")
    val lastMessage: StateFlow<String> = _lastMessage.asStateFlow()

    init {
        // Start background state decay
        viewModelScope.launch {
            while (true) {
                delay(60000) // update every minute
                updateStateOverTime()
            }
        }
    }

    private fun updateStateOverTime() {
        val current = catState.value
        val updated = current.copy(
            hunger = (current.hunger - 2).coerceAtLeast(0),
            energy = (current.energy - 1).coerceAtLeast(0),
            hygiene = (current.hygiene - 1).coerceAtLeast(0),
            mood = (current.mood - 1).coerceAtLeast(0),
            lastUpdated = System.currentTimeMillis()
        )
        viewModelScope.launch { repository.updateCatState(updated) }
    }

    fun feed() {
        updateAction("nakrmení") { it.copy(hunger = (it.hunger + 20).coerceAtMost(100)) }
    }

    fun play() {
        updateAction("hraní") { 
            it.copy(
                mood = (it.mood + 20).coerceAtMost(100),
                energy = (it.energy - 15).coerceAtLeast(0)
            )
        }
    }

    fun sleep() {
        updateAction("spaní") { it.copy(energy = (it.energy + 30).coerceAtMost(100)) }
    }

    fun wash() {
        updateAction("mytí") { it.copy(hygiene = 100) }
    }

    fun pet() {
        updateAction("pohlazení") { it.copy(mood = (it.mood + 10).coerceAtMost(100)) }
    }

    private val _storyText = MutableStateFlow<String?>(null)
    val storyText: StateFlow<String?> = _storyText.asStateFlow()

    fun generateStory() {
        if (!settings.value.storiesEnabled) return
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.service.generateStory(
                    cz.kocika.game.data.api.StoryRequest(
                        mood = catState.value.mood,
                        style = "regular"
                    )
                )
                _storyText.value = response.storyText
            } catch (e: Exception) {
                _storyText.value = "Byl jednou jeden..."
            }
        }
    }

    fun clearStory() {
        _storyText.value = null
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    private fun updateAction(actionName: String, transform: (CatState) -> CatState) {
        viewModelScope.launch {
            val currentState = catState.value
            val newState = transform(currentState)
            repository.updateCatState(newState)
            if (settings.value.aiEnabled) {
                fetchCatResponse(newState, actionName)
            } else {
                _lastMessage.value = "Mňau!"
            }
        }
    }

    private suspend fun fetchCatResponse(state: CatState, action: String) {
        try {
            val response = RetrofitClient.service.getCatResponse(
                CatStatusRequest(
                    hunger = state.hunger,
                    energy = state.energy,
                    hygiene = state.hygiene,
                    mood = state.mood,
                    health = state.health,
                    lastAction = action
                )
            )
            _lastMessage.value = response.message
        } catch (e: Exception) {
            _lastMessage.value = "Mňau!" // Fallback
        }
    }
}

class CatViewModelFactory(private val repository: CatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
