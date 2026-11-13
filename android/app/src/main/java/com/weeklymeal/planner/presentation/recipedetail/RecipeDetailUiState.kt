package com.weeklymeal.planner.presentation.recipedetail

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.presentation.app.PlanUi
import java.time.LocalDate

data class RecipeDetailUiState(
    val plan: PlanUi = PlanUi(),
    val recipe: Recipe? = null,
    val date: LocalDate? = null,
    val servings: Int = 2,
    val ingredients: List<IngredientRequirement> = emptyList(),
    val dayPicker: Boolean = false,
    val pendingDate: LocalDate? = null,
    val deleteDialog: Boolean = false
)
