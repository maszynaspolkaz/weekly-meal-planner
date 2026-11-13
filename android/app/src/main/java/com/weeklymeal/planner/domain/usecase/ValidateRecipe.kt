package com.weeklymeal.planner.domain.usecase

import com.weeklymeal.planner.domain.model.*

class ValidateRecipe {
    operator fun invoke(recipe: Recipe, catalog: List<IngredientDefinition>): Boolean {
        if (recipe.id.isBlank() || recipe.title.value.trim().length !in 1..60 || recipe.baseServings !in 1..12 || recipe.minutes !in 5..180 || recipe.minutes % 5 != 0) return false
        if (recipe.ingredients.size !in 1..40 || recipe.steps.size !in 1..20 || recipe.steps.any { it.value.trim().length !in 1..500 }) return false
        return recipe.ingredients.groupBy { it.ingredientId }.all { (id, lines) ->
            val ingredient = catalog.find { it.id == id } ?: return@all false
            val limit = if (ingredient.measurement == MeasurementType.COUNT) 1000L else 100000L
            lines.all { it.amount in 1..limit } && lines.sumOf { it.amount } <= limit
        }
    }

    fun normalize(recipe: Recipe): Recipe = recipe.copy(
        title = recipe.title.copy(value = recipe.title.value.trim()),
        ingredients = recipe.ingredients.groupBy { it.ingredientId }.map { (id, lines) -> RecipeIngredient(id, lines.sumOf { it.amount }) },
        steps = recipe.steps.map { it.copy(value = it.value.trim()) }
    )
}
