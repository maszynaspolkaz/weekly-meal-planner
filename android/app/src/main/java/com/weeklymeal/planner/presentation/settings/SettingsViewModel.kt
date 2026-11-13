package com.weeklymeal.planner.presentation.settings

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class SettingsViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()
    init {
        observe { plan ->
            _state.update {
                val stored = plan.data?.weekStart ?: it.stored
                it.copy(plan = plan.copy(saving = it.plan.saving), stored = stored, selected = if (it.dirty) it.selected else stored)
            }
        }
    }
    fun select(value: WeekStart) { _state.update { it.copy(selected = value, savedFeedback = false) } }
    fun save(leave: Boolean = false) {
        val selected = _state.value.selected
        write({ saving, error -> _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error)) } }, {
            _state.update { it.copy(stored = selected, selected = selected, savedFeedback = true, discardDialog = false) }
            if (leave) super.back()
        }) { it.copy(weekStart = selected, selectedWeek = weekContaining(if (it.followCurrentWeek) LocalDate.now() else it.selectedWeek, selected), checks = emptyMap()) }
    }
    override fun back() {
        if (_state.value.plan.saving) return
        if (_state.value.dirty) _state.update { it.copy(discardDialog = true) } else super.back()
    }
    fun keepEditing() {
        if (_state.value.plan.saving) return
        _state.update { it.copy(discardDialog = false) }
    }
    fun discard() {
        if (_state.value.plan.saving) return
        discardFailure()
        _state.update { it.copy(selected = it.stored, discardDialog = false, plan = current()) }
        super.back()
    }
}
