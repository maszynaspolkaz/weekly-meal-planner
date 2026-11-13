package com.weeklymeal.planner.core.text

import com.weeklymeal.planner.R

fun labelResource(key: String): Int = when (key) {
    "ingredient_broccoli" -> R.string.ingredient_broccoli
    "ingredient_carrot" -> R.string.ingredient_carrot
    "ingredient_tomato" -> R.string.ingredient_tomato
    "ingredient_spinach" -> R.string.ingredient_spinach
    "ingredient_onion" -> R.string.ingredient_onion
    "ingredient_garlic" -> R.string.ingredient_garlic
    "ingredient_lemon" -> R.string.ingredient_lemon
    "ingredient_chicken" -> R.string.ingredient_chicken
    "ingredient_salmon" -> R.string.ingredient_salmon
    "ingredient_feta" -> R.string.ingredient_feta
    "ingredient_chickpeas" -> R.string.ingredient_chickpeas
    "ingredient_chopped_tomatoes" -> R.string.ingredient_chopped_tomatoes
    "ingredient_pasta" -> R.string.ingredient_pasta
    "ingredient_rice" -> R.string.ingredient_rice
    "ingredient_couscous" -> R.string.ingredient_couscous
    "ingredient_olive_oil" -> R.string.ingredient_olive_oil
    "ingredient_salt" -> R.string.ingredient_salt
    "recipe_chickpea_tray" -> R.string.recipe_chickpea_tray
    "recipe_lemon_chicken" -> R.string.recipe_lemon_chicken
    "recipe_tomato_pasta" -> R.string.recipe_tomato_pasta
    "recipe_salmon_rice" -> R.string.recipe_salmon_rice
    "recipe_chickpea_couscous" -> R.string.recipe_chickpea_couscous
    "recipe_carrot_rice" -> R.string.recipe_carrot_rice
    "recipe_chickpea_tray_step_1" -> R.string.recipe_chickpea_tray_step_1
    "recipe_chickpea_tray_step_2" -> R.string.recipe_chickpea_tray_step_2
    "recipe_chickpea_tray_step_3" -> R.string.recipe_chickpea_tray_step_3
    "recipe_lemon_chicken_step_1" -> R.string.recipe_lemon_chicken_step_1
    "recipe_lemon_chicken_step_2" -> R.string.recipe_lemon_chicken_step_2
    "recipe_lemon_chicken_step_3" -> R.string.recipe_lemon_chicken_step_3
    "recipe_tomato_pasta_step_1" -> R.string.recipe_tomato_pasta_step_1
    "recipe_tomato_pasta_step_2" -> R.string.recipe_tomato_pasta_step_2
    "recipe_tomato_pasta_step_3" -> R.string.recipe_tomato_pasta_step_3
    "recipe_salmon_rice_step_1" -> R.string.recipe_salmon_rice_step_1
    "recipe_salmon_rice_step_2" -> R.string.recipe_salmon_rice_step_2
    "recipe_salmon_rice_step_3" -> R.string.recipe_salmon_rice_step_3
    "recipe_chickpea_couscous_step_1" -> R.string.recipe_chickpea_couscous_step_1
    "recipe_chickpea_couscous_step_2" -> R.string.recipe_chickpea_couscous_step_2
    "recipe_chickpea_couscous_step_3" -> R.string.recipe_chickpea_couscous_step_3
    "recipe_carrot_rice_step_1" -> R.string.recipe_carrot_rice_step_1
    "recipe_carrot_rice_step_2" -> R.string.recipe_carrot_rice_step_2
    "recipe_carrot_rice_step_3" -> R.string.recipe_carrot_rice_step_3
    else -> R.string.app_name
}
