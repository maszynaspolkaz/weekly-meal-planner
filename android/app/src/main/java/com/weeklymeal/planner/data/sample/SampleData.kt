package com.weeklymeal.planner.data.sample

import com.weeklymeal.planner.domain.model.*

object SampleData {
    private fun text(key: String) = Label(key, true)
    fun create(): PlannerData {
        val catalog = listOf(
            IngredientDefinition("broccoli", text("ingredient_broccoli"), MeasurementType.MASS, StoreSection.PRODUCE),
            IngredientDefinition("carrot", text("ingredient_carrot"), MeasurementType.MASS, StoreSection.PRODUCE),
            IngredientDefinition("tomato", text("ingredient_tomato"), MeasurementType.MASS, StoreSection.PRODUCE),
            IngredientDefinition("spinach", text("ingredient_spinach"), MeasurementType.MASS, StoreSection.PRODUCE),
            IngredientDefinition("onion", text("ingredient_onion"), MeasurementType.MASS, StoreSection.PRODUCE),
            IngredientDefinition("garlic", text("ingredient_garlic"), MeasurementType.COUNT, StoreSection.PRODUCE),
            IngredientDefinition("lemon", text("ingredient_lemon"), MeasurementType.COUNT, StoreSection.PRODUCE),
            IngredientDefinition("chicken", text("ingredient_chicken"), MeasurementType.MASS, StoreSection.MEAT_FISH),
            IngredientDefinition("salmon", text("ingredient_salmon"), MeasurementType.MASS, StoreSection.MEAT_FISH),
            IngredientDefinition("feta", text("ingredient_feta"), MeasurementType.MASS, StoreSection.DAIRY_EGGS),
            IngredientDefinition("chickpeas", text("ingredient_chickpeas"), MeasurementType.MASS, StoreSection.CANNED),
            IngredientDefinition("chopped_tomatoes", text("ingredient_chopped_tomatoes"), MeasurementType.MASS, StoreSection.CANNED),
            IngredientDefinition("pasta", text("ingredient_pasta"), MeasurementType.MASS, StoreSection.GRAINS),
            IngredientDefinition("rice", text("ingredient_rice"), MeasurementType.MASS, StoreSection.GRAINS),
            IngredientDefinition("couscous", text("ingredient_couscous"), MeasurementType.MASS, StoreSection.GRAINS),
            IngredientDefinition("olive_oil", text("ingredient_olive_oil"), MeasurementType.VOLUME, StoreSection.OILS_SPICES),
            IngredientDefinition("salt", text("ingredient_salt"), MeasurementType.MASS, StoreSection.OILS_SPICES)
        )
        fun recipe(id: String, category: Category, minutes: Int, image: String, vararg lines: Pair<String, Long>) = Recipe(
            id, text(id), category, minutes, 2, image,
            lines.map { RecipeIngredient(it.first, it.second) },
            (1..3).map { text("${id}_step_$it") }
        )
        val recipes = listOf(
            recipe("recipe_chickpea_tray", Category.VEGETARIAN, 30, "sprite_1", "chickpeas" to 240L, "broccoli" to 300L, "carrot" to 200L, "olive_oil" to 15L, "salt" to 2L),
            recipe("recipe_lemon_chicken", Category.CHICKEN, 30, "sprite_2", "chicken" to 300L, "rice" to 150L, "broccoli" to 200L, "lemon" to 1L, "olive_oil" to 15L, "salt" to 2L),
            recipe("recipe_tomato_pasta", Category.VEGETARIAN, 20, "sprite_3", "pasta" to 180L, "chopped_tomatoes" to 400L, "spinach" to 100L, "garlic" to 2L, "olive_oil" to 15L, "salt" to 2L),
            recipe("recipe_salmon_rice", Category.FISH, 25, "sprite_4", "salmon" to 300L, "rice" to 150L, "broccoli" to 250L, "lemon" to 1L, "olive_oil" to 10L, "salt" to 2L),
            recipe("recipe_chickpea_couscous", Category.VEGETARIAN, 15, "sprite_5", "couscous" to 160L, "chickpeas" to 240L, "tomato" to 250L, "spinach" to 80L, "feta" to 100L, "lemon" to 1L, "olive_oil" to 15L),
            recipe("recipe_carrot_rice", Category.VEGETARIAN, 25, "sprite_1", "rice" to 150L, "carrot" to 250L, "chickpeas" to 240L, "onion" to 100L, "olive_oil" to 15L, "salt" to 2L)
        )
        return PlannerData(recipes = recipes, ingredientCatalog = catalog)
    }
}
