package com.weeklymeal.planner.presentation.game

import androidx.lifecycle.viewModelScope
import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(repository: PlannerRepository, aggregate: AggregateIngredients) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(GameUiState())
    val state = _state.asStateFlow()
    private val scrollChannel = Channel<Float>(Channel.CONFLATED)
    val scrollEvents = scrollChannel.receiveAsFlow()
    private var scrollJob: Job? = null
    private var scrollVelocity = 0f
    private var pendingNavigation: Screen? = null
    private var feedbackJob: Job? = null
    private fun showSaved(date: LocalDate) {
        feedbackJob?.cancel()
        _state.update { it.copy(lastChangedDay = date) }
        feedbackJob = viewModelScope.launch {
            delay(1000)
            _state.update { it.copy(lastChangedDay = null, plan = it.plan.copy(saved = false)) }
        }
    }
    fun autoScroll(velocity: Float) {
        scrollVelocity = velocity
        if (velocity == 0f) { scrollJob?.cancel(); scrollJob = null; return }
        if (scrollJob?.isActive == true) return
        scrollJob = viewModelScope.launch {
            while (isActive) { delay(16); scrollChannel.trySend(scrollVelocity * 0.016f) }
        }
    }
    fun action(action: GameAction) {
        when (action) {
            is GameAction.ShiftWeek -> shiftWeek(action.offset)
            is GameAction.AddDate -> navigate(Screen.Recipes(action.date))
            is GameAction.Focus -> focus(action.date)
            is GameAction.ViewDinner -> {
                val dinner = _state.value.plan.data?.dinners?.get(action.date)
                if (dinner != null) { dismiss(); navigate(Screen.RecipeDetail(dinner.snapshot.sourceRecipeId, action.date)) }
            }
            is GameAction.ViewRecipe -> navigate(Screen.RecipeDetail(action.recipe.id))
            is GameAction.Pick -> pick(action.recipe)
            is GameAction.Choose -> choose(action.date)
            is GameAction.Drop -> {
                val servings = action.source?.let { _state.value.plan.data?.dinners?.get(it)?.servings } ?: action.recipe.baseServings
                assign(action.recipe, action.date, servings, action.source)
            }
            is GameAction.Servings -> servings(action.value)
            is GameAction.Scroll -> autoScroll(action.velocity)
            GameAction.Today -> today()
            GameAction.Recipes -> navigate(Screen.Recipes())
            GameAction.Build -> build()
            GameAction.Back -> back()
            GameAction.Move -> move()
            GameAction.Replace -> {
                val date = _state.value.focusedDay
                dismiss()
                if (date != null) navigate(Screen.Recipes(date))
            }
            GameAction.RemoveAsk -> removeAsk(_state.value.focusedDay)
            GameAction.Remove -> remove()
            GameAction.Confirm -> confirmReplacement()
            GameAction.CopyAsk -> copyPrevious()
            GameAction.CopyConfirm -> copyPrevious(true)
            GameAction.Dismiss -> dismiss()
            GameAction.Retry -> retry()
            GameAction.Discard -> discard()
            GameAction.Leave -> leave()
        }
    }
    init { observe { plan -> _state.update { it.copy(plan = plan.copy(saving = it.plan.saving)) } } }
    private fun progress(saving: Boolean, error: Boolean) { _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error, saved = !saving && !error, checksChanged = requirementsChanged)) } }
    fun shiftWeek(offset: Long) {
        if (_state.value.plan.error || _state.value.plan.saving) return
        write(::progress) { it.copy(selectedWeek = it.selectedWeek.plusDays(offset * 7), followCurrentWeek = false) }
    }
    fun today() { write(::progress) { it.copy(selectedWeek = weekContaining(LocalDate.now(), it.weekStart), followCurrentWeek = true) } }
    fun pick(recipe: Recipe) { _state.update { it.copy(pickerRecipe = recipe, moveSource = null) } }
    fun choose(date: LocalDate) {
        val state = _state.value
        val recipe = state.pickerRecipe ?: return
        val servings = state.moveSource?.let { state.plan.data?.dinners?.get(it)?.servings } ?: recipe.baseServings
        assign(recipe, date, servings, state.moveSource)
    }
    fun assign(recipe: Recipe, date: LocalDate, servings: Int = recipe.baseServings, source: LocalDate? = null) {
        if (source == date || _state.value.plan.saving || _state.value.plan.error) return
        val pending = PendingAssignment(recipe, date, servings.coerceIn(1, 12), source)
        _state.update { it.copy(pickerRecipe = null, focusedDay = null, moveSource = null) }
        if (_state.value.plan.data?.dinners?.containsKey(date) == true) _state.update { it.copy(pending = pending) } else commitAssignment(pending)
    }
    private fun commitAssignment(pending: PendingAssignment) {
        _state.update { it.copy(pending = null) }
        write(::progress, { showSaved(pending.date) }) { data ->
            val dinner = PlannedDinner(pending.date, RecipeSnapshot(pending.recipe.id, pending.recipe), pending.servings)
            data.copy(dinners = (data.dinners - listOfNotNull(pending.source)) + (pending.date to dinner))
        }
    }
    fun confirmReplacement() { _state.value.pending?.let(::commitAssignment) }
    fun focus(date: LocalDate?) { _state.update { it.copy(focusedDay = date) } }
    fun servings(value: Int) {
        val date = _state.value.focusedDay ?: return
        if (value !in 1..12) return
        write(::progress, { showSaved(date) }) { data -> data.dinners[date]?.let { data.copy(dinners = data.dinners + (date to it.copy(servings = value))) } ?: data }
    }
    fun move() {
        val date = _state.value.focusedDay ?: return
        val dinner = _state.value.plan.data?.dinners?.get(date) ?: return
        _state.update { it.copy(focusedDay = null, pickerRecipe = dinner.snapshot.recipe, moveSource = date) }
    }
    fun removeAsk(date: LocalDate?) { _state.update { it.copy(removeDate = date, focusedDay = null) } }
    fun remove() {
        val date = _state.value.removeDate ?: return
        _state.update { it.copy(removeDate = null) }
        write(::progress, { showSaved(date) }) { it.copy(dinners = it.dinners - date) }
    }
    fun copyPrevious(confirm: Boolean = false) {
        val data = _state.value.plan.data ?: return
        val week = data.selectedWeek
        val previous = data.dinners.values.filter { !it.date.isBefore(week.minusDays(7)) && it.date.isBefore(week) }
        if (previous.isEmpty()) { _state.update { it.copy(noPrevious = true) }; return }
        if (!confirm && previous.any { data.dinners.containsKey(it.date.plusDays(7)) }) { _state.update { it.copy(copyConfirmation = true) }; return }
        _state.update { it.copy(copyConfirmation = false, noPrevious = false) }
        write(::progress) { current -> current.copy(dinners = current.dinners + previous.associate { it.date.plusDays(7) to it.copy(date = it.date.plusDays(7)) }) }
    }
    fun build() {
        val plan = _state.value.plan
        val data = plan.data ?: return
        if (plan.summary.dinners.isNotEmpty() && !plan.saving && !plan.error) navigate(Screen.GameOver(data.selectedWeek, data.revision))
    }
    fun dismiss() { _state.update { it.copy(pickerRecipe = null, pending = null, focusedDay = null, removeDate = null, moveSource = null, copyConfirmation = false, noPrevious = false, leaveConfirmation = false) } }
    override fun back() {
        pendingNavigation = null
        val state = _state.value
        when {
            state.plan.saving -> Unit
            state.pickerRecipe != null || state.pending != null || state.focusedDay != null || state.removeDate != null || state.copyConfirmation || state.noPrevious -> dismiss()
            state.plan.error -> _state.update { it.copy(leaveConfirmation = true) }
            else -> super.back()
        }
    }
    fun discard() {
        discardFailure()
        _state.update { it.copy(plan = current(), leaveConfirmation = false) }
    }
    override fun navigate(screen: Screen) {
        val plan = _state.value.plan
        if (plan.saving) return
        if (plan.error) { pendingNavigation = screen; _state.update { it.copy(leaveConfirmation = true) } }
        else super.navigate(screen)
    }
    fun leave() {
        val target = pendingNavigation
        pendingNavigation = null
        discard()
        if (target == null) super.back() else super.navigate(target)
    }
    fun pause() { autoScroll(0f); feedbackJob?.cancel(); _state.update { it.copy(paused = true, lastChangedDay = null) } }
    fun resume() {
        _state.update { it.copy(paused = false) }
        val data = _state.value.plan.data ?: return
        if (data.followCurrentWeek && data.selectedWeek != weekContaining(LocalDate.now(), data.weekStart) && !_state.value.plan.error) today()
    }
}
