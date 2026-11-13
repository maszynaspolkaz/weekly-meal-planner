package com.weeklymeal.planner.presentation.recipes

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.presentation.app.PlanUi
import java.time.LocalDate

enum class RecipeSort { NAME, QUICKEST, RECENT }
sealed interface EditorAction {
    data class Title(val value: String) : EditorAction
    data class SetCategory(val value: Category) : EditorAction
    data class Duration(val value: Int) : EditorAction
    data class Servings(val value: Int) : EditorAction
    data class Image(val value: String) : EditorAction
    data class AddIngredient(val value: IngredientDefinition) : EditorAction
    data class RemoveIngredient(val id: String) : EditorAction
    data class Amount(val id: String, val direction: Int) : EditorAction
    data class Unit(val id: String) : EditorAction
    data class Step(val index: Int, val value: String) : EditorAction
    data class MoveStep(val index: Int, val direction: Int) : EditorAction
    data class RemoveStep(val index: Int) : EditorAction
    data class IngredientQuery(val value: String) : EditorAction
    data class NewIngredient(val value: IngredientDraft) : EditorAction
    data object AddStep : EditorAction
    data object OpenIngredients : EditorAction
    data object CloseIngredients : EditorAction
    data object CreateIngredient : EditorAction
    data object SaveIngredient : EditorAction
    data object Save : EditorAction
    data object Cancel : EditorAction
    data object Discard : EditorAction
    data object KeepEditing : EditorAction
}
data class IngredientDraft(val name: String = "", val measurement: MeasurementType = MeasurementType.MASS, val section: StoreSection = StoreSection.PRODUCE)
data class RecipesUiState(
    val plan: PlanUi = PlanUi(),
    val visible: List<Recipe> = emptyList(),
    val query: String = "",
    val category: Category? = null,
    val sort: RecipeSort = RecipeSort.NAME,
    val targetDate: LocalDate? = null,
    val editor: Recipe? = null,
    val original: Recipe? = null,
    val extraIngredients: List<IngredientDefinition> = emptyList(),
    val picker: Boolean = false,
    val ingredientQuery: String = "",
    val ingredientDraft: IngredientDraft? = null,
    val ingredientError: Boolean = false,
    val invalid: Boolean = false,
    val discardDialog: Boolean = false,
    val deleteCandidate: Recipe? = null,
    val dayRecipe: Recipe? = null,
    val pendingDate: LocalDate? = null,
    val largeUnits: Set<String> = emptySet()
) {
    val dirty: Boolean get() = editor != original || extraIngredients.isNotEmpty()
}
