package com.weeklymeal.planner.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlanUi(
    val data: PlannerData? = null,
    val summary: WeekSummary = WeekSummary(LocalDate.now()),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: Boolean = false,
    val saved: Boolean = false,
    val checksChanged: Boolean = false
)

abstract class PlannerViewModel(protected val repository: PlannerRepository, protected val aggregate: AggregateIngredients) : ViewModel() {
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var failedAction: (() -> Unit)? = null
    private var writing = false
    private val hasFailedWrite: Boolean get() = failedAction != null && !writing
    protected var requirementsChanged = false
    protected fun emit(event: AppEvent) { channel.trySend(event) }
    open fun navigate(screen: Screen) = emit(AppEvent.Navigate(screen))
    fun planDinners() = emit(AppEvent.Again)
    open fun back() = emit(AppEvent.Back)
    fun retry() { failedAction?.invoke() }
    fun discardFailure() { failedAction = null }
    protected fun observe(week: () -> LocalDate? = { null }, update: (PlanUi) -> Unit) {
        viewModelScope.launch {
            repository.state.collect { store ->
                val data = store.data
                update(PlanUi(data, if (data == null) WeekSummary(LocalDate.now()) else aggregate.summary(data, week()?.let { weekContaining(it, data.weekStart) } ?: data.selectedWeek), store.loading, error = store.error || hasFailedWrite))
            }
        }
    }
    protected fun current(week: LocalDate? = null): PlanUi {
        val store = repository.state.value
        val data = store.data
        return PlanUi(data, if (data == null) WeekSummary(LocalDate.now()) else aggregate.summary(data, week?.let { weekContaining(it, data.weekStart) } ?: data.selectedWeek), store.loading, error = store.error || hasFailedWrite)
    }
    protected fun write(
        progress: (Boolean, Boolean) -> Unit,
        success: (PlannerData) -> Unit = {},
        transform: (PlannerData) -> PlannerData
    ) {
        if (writing) return
        failedAction = { write(progress, success, transform) }
        writing = true
        requirementsChanged = false
        progress(true, false)
        viewModelScope.launch {
            try {
                val previous = repository.state.value.data?.checks.orEmpty()
                val next = repository.update(transform)
                requirementsChanged = previous.any { (week, checks) -> checks.keys.any { it !in next.checks[week].orEmpty() } }
                failedAction = null
                progress(false, false)
                success(next)
                emit(AppEvent.Saved)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                progress(false, true)
            } finally {
                writing = false
            }
        }
    }
    fun reload() { viewModelScope.launch { repository.load() } }
}
