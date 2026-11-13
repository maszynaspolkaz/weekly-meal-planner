package com.weeklymeal.planner.presentation.gameover

import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class GameOverViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(GameOverUiState())
    val state = _state.asStateFlow()
    private var week: LocalDate? = null
    init { observe({ week }) { plan -> _state.update { it.copy(plan = plan) } } }
    fun enter(value: LocalDate) { week = value; _state.update { it.copy(plan = current(value)) } }
    fun again() = emit(AppEvent.Again)
    fun menu() = emit(AppEvent.Menu)
}
