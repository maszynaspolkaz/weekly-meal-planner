package com.weeklymeal.planner.presentation.game

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.presentation.app.PlanUi
import java.time.LocalDate

data class PendingAssignment(val recipe: Recipe, val date: LocalDate, val servings: Int, val source: LocalDate? = null)
sealed interface GameAction {
    data class ShiftWeek(val offset: Long) : GameAction
    data class AddDate(val date: LocalDate) : GameAction
    data class Focus(val date: LocalDate) : GameAction
    data class ViewDinner(val date: LocalDate) : GameAction
    data class ViewRecipe(val recipe: Recipe) : GameAction
    data class Pick(val recipe: Recipe) : GameAction
    data class Choose(val date: LocalDate) : GameAction
    data class Drop(val recipe: Recipe, val date: LocalDate, val source: LocalDate?) : GameAction
    data class Servings(val value: Int) : GameAction
    data class Scroll(val velocity: Float) : GameAction
    data object Today : GameAction
    data object Recipes : GameAction
    data object Build : GameAction
    data object Back : GameAction
    data object Move : GameAction
    data object Replace : GameAction
    data object RemoveAsk : GameAction
    data object Remove : GameAction
    data object Confirm : GameAction
    data object CopyAsk : GameAction
    data object CopyConfirm : GameAction
    data object Dismiss : GameAction
    data object Retry : GameAction
    data object Discard : GameAction
    data object Leave : GameAction
}
data class GameUiState(
    val plan: PlanUi = PlanUi(),
    val pickerRecipe: Recipe? = null,
    val focusedDay: LocalDate? = null,
    val pending: PendingAssignment? = null,
    val removeDate: LocalDate? = null,
    val moveSource: LocalDate? = null,
    val copyConfirmation: Boolean = false,
    val noPrevious: Boolean = false,
    val leaveConfirmation: Boolean = false,
    val lastChangedDay: LocalDate? = null,
    val paused: Boolean = false
)
