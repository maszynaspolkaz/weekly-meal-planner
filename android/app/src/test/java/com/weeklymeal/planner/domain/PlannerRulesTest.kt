package com.weeklymeal.planner.domain

import com.weeklymeal.planner.data.sample.SampleData
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.domain.usecase.ReconcileRequirementChecks
import com.weeklymeal.planner.domain.usecase.ValidateRecipe
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlannerRulesTest {
    private val aggregate = AggregateIngredients()
    private val week = LocalDate.of(2026, 11, 9)
    private fun dinner(recipe: Recipe, day: Int, servings: Int = 2) = PlannedDinner(week.plusDays(day.toLong()), RecipeSnapshot(recipe.id, recipe), servings)

    @Test
    fun workedWeekMatchesEveryRequiredQuantity() {
        val data = SampleData.create()
        val order = listOf(0, 1, 2, 4, 3, 5, 2)
        val dinners = order.mapIndexed { day, recipe -> dinner(data.recipes[recipe], day) }
        val plan = data.copy(selectedWeek = week, dinners = dinners.associateBy { it.date })
        val summary = aggregate.summary(plan)
        assertEquals(7, summary.dinners.size)
        assertEquals(14, summary.portions)
        assertEquals(6, summary.requirements.map { it.ingredient.section }.distinct().size)
        assertEquals(mapOf(
            "broccoli" to 750L, "carrot" to 450L, "tomato" to 250L, "spinach" to 280L,
            "onion" to 100L, "garlic" to 4L, "lemon" to 3L, "chicken" to 300L,
            "salmon" to 300L, "feta" to 100L, "chickpeas" to 720L,
            "chopped_tomatoes" to 800L, "pasta" to 360L, "rice" to 450L,
            "couscous" to 160L, "olive_oil" to 100L, "salt" to 12L
        ), summary.requirements.associate { it.ingredient.id to it.amount })
        val key = checkKey(week, WeekStart.MONDAY)
        val checked = plan.copy(checks = mapOf(key to mapOf(
            "olive_oil" to RequirementCheck(100L, 1L, atHome = true),
            "salt" to RequirementCheck(12L, 1L, atHome = true),
            "broccoli" to RequirementCheck(750L, 1L, purchased = true)
        )))
        assertEquals(14, aggregate.summary(checked).remaining)
        assertEquals(2, aggregate.summary(checked).atHome)
        assertEquals(1, aggregate.summary(checked).bought)
        val buying = aggregate.summary(checked).requirements.filter { checked.checks[key]?.get(it.ingredient.id)?.atHome != true }
        assertEquals(15, buying.size)
        assertTrue(buying.none { it.ingredient.section == StoreSection.OILS_SPICES })
    }

    @Test
    fun sixOneSixthContributionsRoundOnlyOnce() {
        val data = SampleData.create()
        val recipe = data.recipes.first().copy(baseServings = 6, ingredients = listOf(RecipeIngredient("garlic", 1L)))
        val requirements = aggregate((0..5).map { dinner(recipe, it, 1) }, data.ingredientCatalog)
        assertEquals(1L, requirements.single().amount)
    }

    @Test
    fun partialCanonicalAmountsAlwaysRoundUp() {
        val data = SampleData.create()
        val recipe = data.recipes.first().copy(baseServings = 3, ingredients = listOf(RecipeIngredient("olive_oil", 1L)))
        assertEquals(1L, aggregate(listOf(dinner(recipe, 0, 1), dinner(recipe, 1, 1)), data.ingredientCatalog).single().amount)
    }

    @Test
    fun checksAreRetainedOnlyForUnchangedCanonicalAmounts() {
        val data = SampleData.create()
        val recipe = data.recipes.first()
        val dinner = dinner(recipe, 0)
        val key = checkKey(week, WeekStart.MONDAY)
        val checks = mapOf("broccoli" to RequirementCheck(300L, 1L, atHome = true))
        val plan = data.copy(selectedWeek = week, dinners = mapOf(dinner.date to dinner), checks = mapOf(key to checks))
        val reconcile = ReconcileRequirementChecks(aggregate)
        assertEquals(checks, reconcile(plan).checks[key])
        val moved = dinner.copy(date = week.plusDays(3))
        assertEquals(checks, reconcile(plan.copy(dinners = mapOf(moved.date to moved))).checks[key])
        assertTrue(reconcile(plan.copy(dinners = mapOf(dinner.date to dinner.copy(servings = 1)))).checks.isEmpty())
        assertTrue(reconcile(plan.copy(dinners = mapOf(dinner.date to dinner.copy(servings = 3)))).checks.isEmpty())
        assertTrue(reconcile(plan.copy(dinners = emptyMap())).checks.isEmpty())
    }

    @Test
    fun removingAndEditingTheLibraryPreservesDinnerSnapshots() {
        val data = SampleData.create()
        val dinner = dinner(data.recipes.first(), 0)
        val plan = data.copy(selectedWeek = week, recipes = emptyList(), dinners = mapOf(dinner.date to dinner))
        assertEquals(300L, aggregate.summary(plan).requirements.single { it.ingredient.id == "broccoli" }.amount)
        assertEquals(data.recipes.first().title, dinner.snapshot.recipe.title)
    }

    @Test
    fun recipeValidationCombinesDuplicatesAndRejectsInvalidInputs() {
        val data = SampleData.create()
        val validator = ValidateRecipe()
        assertTrue(data.recipes.all { validator(it, data.ingredientCatalog) })
        val recipe = data.recipes.first().copy(ingredients = listOf(RecipeIngredient("salt", 1L), RecipeIngredient("salt", 2L)))
        assertEquals(listOf(RecipeIngredient("salt", 3L)), validator.normalize(recipe).ingredients)
        assertFalse(validator(recipe.copy(title = Label(" ")), data.ingredientCatalog))
        assertFalse(validator(recipe.copy(baseServings = 13), data.ingredientCatalog))
        assertFalse(validator(recipe.copy(ingredients = listOf(RecipeIngredient("salt", 100001L))), data.ingredientCatalog))
        assertFalse(validator(recipe.copy(ingredients = listOf(RecipeIngredient("missing", 1L))), data.ingredientCatalog))
        assertFalse(validator(recipe.copy(steps = listOf(Label(""))), data.ingredientCatalog))
    }

    @Test
    fun weekPreferenceReanchorsWithoutMovingDinnerDates() {
        val date = LocalDate.of(2026, 11, 13)
        assertEquals(LocalDate.of(2026, 11, 9), weekContaining(date, WeekStart.MONDAY))
        assertEquals(LocalDate.of(2026, 11, 8), weekContaining(date, WeekStart.SUNDAY))
    }
}
