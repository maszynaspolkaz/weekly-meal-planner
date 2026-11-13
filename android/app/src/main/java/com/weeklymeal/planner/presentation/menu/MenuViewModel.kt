package com.weeklymeal.planner.presentation.menu

import androidx.lifecycle.viewModelScope
import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.weeklymeal.planner.domain.model.weekContaining
import java.time.LocalDate

class MenuViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(MenuUiState())
    val state = _state.asStateFlow()
    init { observe { plan -> _state.update { it.copy(plan = plan) } } }
    fun play() { if (_state.value.plan.data != null) navigate(Screen.Game) }
    fun resume() {
        val data = _state.value.plan.data ?: return
        val today = weekContaining(LocalDate.now(), data.weekStart)
        if (data.followCurrentWeek && today != data.selectedWeek) write({ saving, error -> _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error)) } }) { it.copy(selectedWeek = today) }
    }
    fun resetDialog(show: Boolean) { _state.update { it.copy(confirmReset = show) } }
    fun reset() {
        resetDialog(false)
        viewModelScope.launch { repository.load(reset = true) }
    }
}
