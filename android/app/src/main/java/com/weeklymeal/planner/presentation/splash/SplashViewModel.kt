package com.weeklymeal.planner.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklymeal.planner.core.config.GameConfig
import com.weeklymeal.planner.domain.repository.PlannerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(repository: PlannerRepository) : ViewModel() {
    private val _state = MutableStateFlow(SplashUiState())
    val state = _state.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            _state.update { it.copy(finished = true) }
        }
    }
}
