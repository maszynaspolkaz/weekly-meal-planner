package com.weeklymeal.planner.presentation.recipedetail

import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class RecipeDetailViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state = _state.asStateFlow()
    private var route: Pair<String, LocalDate?>? = null
    init { observe { plan -> _state.update { it.copy(plan = plan.copy(saving = it.plan.saving)) }; refresh() } }
    fun enter(id: String, date: LocalDate?) {
        val next = id to date
        if (route != next) {
            route = next
            val data = repository.state.value.data
            val recipe = if (date == null) data?.recipes?.find { it.id == id } else data?.dinners?.get(date)?.snapshot?.recipe
            val servings = if (date == null) recipe?.baseServings ?: 2 else data?.dinners?.get(date)?.servings ?: 2
            _state.update { it.copy(recipe = recipe, date = date, servings = servings, dayPicker = false, pendingDate = null, deleteDialog = false) }
        }
        refresh()
    }
    private fun refresh() {
        val (id, date) = route ?: return
        val data = repository.state.value.data ?: return
        val recipe = if (date == null) data.recipes.find { it.id == id } else data.dinners[date]?.snapshot?.recipe
        val ingredients = recipe?.let { aggregate(listOf(PlannedDinner(date ?: data.selectedWeek, RecipeSnapshot(it.id, it), _state.value.servings)), data.ingredientCatalog) }.orEmpty()
        _state.update { it.copy(recipe = recipe, date = date, ingredients = ingredients) }
    }
    fun servings(value: Int) { _state.update { it.copy(servings = value.coerceIn(1, 12)) }; refresh() }
    private fun progress(saving: Boolean, error: Boolean) { _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error, saved = !saving && !error, checksChanged = requirementsChanged)) } }
    fun add() {
        val date = _state.value.date
        if (date != null) choose(date, true) else _state.update { it.copy(dayPicker = true) }
    }
    fun choose(date: LocalDate, confirmed: Boolean = false) {
        val state = _state.value
        val recipe = state.recipe ?: return
        if (!confirmed && state.plan.data?.dinners?.containsKey(date) == true) { _state.update { it.copy(pendingDate = date) }; return }
        _state.update { it.copy(dayPicker = false, pendingDate = null) }
        write(::progress) { it.copy(dinners = it.dinners + (date to PlannedDinner(date, RecipeSnapshot(recipe.id, recipe), state.servings))) }
    }
    fun dismiss() { _state.update { it.copy(dayPicker = false, pendingDate = null, deleteDialog = false) } }
    fun deleteAsk() { _state.update { it.copy(deleteDialog = true) } }
    fun delete() {
        val id = _state.value.recipe?.id ?: return
        dismiss()
        write(::progress, { emit(AppEvent.Back) }) { it.copy(recipes = it.recipes.filterNot { recipe -> recipe.id == id }) }
    }
    override fun back() {
        val state = _state.value
        if (state.dayPicker || state.pendingDate != null || state.deleteDialog) dismiss() else if (!state.plan.saving) super.back()
    }
}
