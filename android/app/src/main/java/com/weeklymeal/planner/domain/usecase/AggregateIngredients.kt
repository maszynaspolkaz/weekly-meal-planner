package com.weeklymeal.planner.domain.usecase

import com.weeklymeal.planner.domain.model.*
import java.time.LocalDate

class AggregateIngredients {
    operator fun invoke(dinners: List<PlannedDinner>, catalog: List<IngredientDefinition>): List<IngredientRequirement> {
        val definitions = catalog.associateBy { it.id }
        val contributions = linkedMapOf<String, MutableList<Contribution>>()
        dinners.forEach { dinner ->
            require(dinner.servings in 1..12)
            val recipe = dinner.snapshot.recipe
            require(recipe.baseServings in 1..12)
            recipe.ingredients.forEach { line ->
                require(definitions.containsKey(line.ingredientId) && line.amount > 0)
                val numerator = line.amount * dinner.servings * (DENOMINATOR / recipe.baseServings)
                contributions.getOrPut(line.ingredientId) { mutableListOf() }.add(Contribution(dinner.date, recipe.title, numerator))
            }
        }
        return contributions.map { (id, values) ->
            val total = values.sumOf { it.numerator }
            IngredientRequirement(definitions.getValue(id), (total + DENOMINATOR - 1) / DENOMINATOR, values.toList())
        }.sortedWith(compareBy({ it.ingredient.section.ordinal }, { it.ingredient.name.value.lowercase() }))
    }

    fun summary(data: PlannerData, week: LocalDate = data.selectedWeek): WeekSummary {
        val dinners = data.dinners.values.filter { !it.date.isBefore(week) && it.date.isBefore(week.plusDays(7)) }.sortedBy { it.date }
        return WeekSummary(week, dinners, invoke(dinners, data.ingredientCatalog), data.checks[checkKey(week, data.weekStart)].orEmpty())
    }

    companion object { const val DENOMINATOR = 27720L }
}
