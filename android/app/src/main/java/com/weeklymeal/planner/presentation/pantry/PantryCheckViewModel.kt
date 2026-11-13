package com.weeklymeal.planner.presentation.pantry

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class PantryCheckViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(PantryCheckUiState())
    val state = _state.asStateFlow()
    private var week: LocalDate? = null
    init { observe({ week }) { plan -> _state.update { it.copy(plan = plan.copy(saving = it.plan.saving)) } } }
    fun enter(value: LocalDate) { week = value; _state.update { it.copy(plan = current(value)) } }
    private fun progress(saving: Boolean, error: Boolean) { _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error, saved = !saving && !error)) } }
    fun toggle(id: String, atHome: Boolean) {
        val routeWeek = week ?: return
        write(::progress) { data ->
            val target = weekContaining(routeWeek, data.weekStart)
            val amount = aggregate.summary(data, target).requirements.find { it.ingredient.id == id }?.amount ?: error("Missing requirement")
            val key = checkKey(target, data.weekStart)
            val checks = data.checks[key].orEmpty() + (id to RequirementCheck(amount, data.revision + 1, atHome = atHome))
            data.copy(checks = data.checks + (key to checks))
        }
    }
    fun details(id: String) { _state.update { it.copy(expanded = if (id in it.expanded) it.expanded - id else it.expanded + id) } }
    fun clearAsk() { _state.update { it.copy(clearDialog = true) } }
    fun clear() {
        val routeWeek = week ?: return
        dismiss()
        write(::progress) { data ->
            val target = weekContaining(routeWeek, data.weekStart)
            val key = checkKey(target, data.weekStart)
            data.copy(checks = data.checks + (key to data.checks[key].orEmpty().mapValues { it.value.copy(atHome = false) }))
        }
    }
    fun dismiss() { _state.update { it.copy(clearDialog = false) } }
    override fun back() { if (_state.value.clearDialog) dismiss() else if (!_state.value.plan.saving) super.back() }
}
