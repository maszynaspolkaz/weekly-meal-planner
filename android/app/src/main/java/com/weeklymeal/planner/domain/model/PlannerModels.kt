package com.weeklymeal.planner.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Label(val value: String, val resource: Boolean = false)
enum class MeasurementType { MASS, VOLUME, COUNT }
enum class StoreSection { PRODUCE, MEAT_FISH, DAIRY_EGGS, CANNED, GRAINS, OILS_SPICES }
enum class Category { VEGETARIAN, CHICKEN, FISH, OTHER }
enum class WeekStart(val day: DayOfWeek) { MONDAY(DayOfWeek.MONDAY), SUNDAY(DayOfWeek.SUNDAY) }
data class IngredientDefinition(val id: String, val name: Label, val measurement: MeasurementType, val section: StoreSection)
data class RecipeIngredient(val ingredientId: String, val amount: Long)
data class Recipe(
    val id: String,
    val title: Label,
    val category: Category,
    val minutes: Int,
    val baseServings: Int,
    val imageKey: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<Label>,
    val updatedAt: Long = 0L
)
data class RecipeSnapshot(val sourceRecipeId: String, val recipe: Recipe)
data class PlannedDinner(val date: LocalDate, val snapshot: RecipeSnapshot, val servings: Int)
data class Contribution(val date: LocalDate, val title: Label, val numerator: Long)
data class IngredientRequirement(val ingredient: IngredientDefinition, val amount: Long, val contributions: List<Contribution>)
data class RequirementCheck(val amount: Long, val revision: Long, val atHome: Boolean = false, val purchased: Boolean = false)
data class PlannerData(
    val schemaVersion: Int = 1,
    val seedVersion: Int = 1,
    val recipes: List<Recipe>,
    val ingredientCatalog: List<IngredientDefinition>,
    val dinners: Map<LocalDate, PlannedDinner> = emptyMap(),
    val checks: Map<String, Map<String, RequirementCheck>> = emptyMap(),
    val weekStart: WeekStart = WeekStart.MONDAY,
    val selectedWeek: LocalDate = weekContaining(LocalDate.now(), WeekStart.MONDAY),
    val revision: Long = 0L,
    val followCurrentWeek: Boolean = true
)
data class StoreState(val data: PlannerData? = null, val loading: Boolean = true, val error: Boolean = false)
data class WeekSummary(
    val week: LocalDate,
    val dinners: List<PlannedDinner> = emptyList(),
    val requirements: List<IngredientRequirement> = emptyList(),
    val checks: Map<String, RequirementCheck> = emptyMap()
) {
    val portions: Int get() = dinners.sumOf { it.servings }
    val atHome: Int get() = requirements.count { checks[it.ingredient.id]?.atHome == true }
    val bought: Int get() = requirements.count { checks[it.ingredient.id]?.purchased == true }
    val remaining: Int get() = requirements.size - atHome - bought
}
fun weekContaining(date: LocalDate, start: WeekStart): LocalDate = date.with(TemporalAdjusters.previousOrSame(start.day))
fun checkKey(week: LocalDate, start: WeekStart): String = "${start.name}:$week"
