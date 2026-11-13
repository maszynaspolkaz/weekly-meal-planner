package com.weeklymeal.planner.presentation.shopping

import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class ShoppingListViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(ShoppingListUiState())
    val state = _state.asStateFlow()
    private var week: LocalDate? = null
    init { observe({ week }) { plan -> _state.update { it.copy(plan = plan.copy(saving = it.plan.saving)) } } }
    fun enter(value: LocalDate) { week = value; _state.update { it.copy(plan = current(value)) } }
    private fun progress(saving: Boolean, error: Boolean) { _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error, saved = !saving && !error)) } }
    fun toggle(id: String, purchased: Boolean) {
        val routeWeek = week ?: return
        write(::progress) { data ->
            val target = weekContaining(routeWeek, data.weekStart)
            val amount = aggregate.summary(data, target).requirements.find { it.ingredient.id == id }?.amount ?: error("Missing requirement")
            val key = checkKey(target, data.weekStart)
            val checks = data.checks[key].orEmpty() + (id to RequirementCheck(amount, data.revision + 1, purchased = purchased))
            data.copy(checks = data.checks + (key to checks))
        }
    }
    fun hide() { _state.update { it.copy(hidePurchased = !it.hidePurchased) } }
    fun details(id: String) { _state.update { it.copy(expanded = if (id in it.expanded) it.expanded - id else it.expanded + id) } }
    fun resetAsk() { _state.update { it.copy(resetDialog = true) } }
    fun reset() {
        val routeWeek = week ?: return
        dismiss()
        write(::progress) { data ->
            val target = weekContaining(routeWeek, data.weekStart)
            val key = checkKey(target, data.weekStart)
            data.copy(checks = data.checks + (key to data.checks[key].orEmpty().mapValues { it.value.copy(purchased = false) }))
        }
    }
    fun done(confirmed: Boolean = false) {
        val plan = _state.value.plan
        val data = plan.data ?: return
        if (plan.saving || plan.error) return
        if (plan.summary.remaining > 0 && !confirmed) _state.update { it.copy(finishDialog = true) }
        else { dismiss(); navigate(Screen.GameOver(plan.summary.week, data.revision)) }
    }
    fun dismiss() { _state.update { it.copy(resetDialog = false, finishDialog = false) } }
    override fun back() { if (_state.value.resetDialog || _state.value.finishDialog) dismiss() else if (!_state.value.plan.saving) super.back() }
}
