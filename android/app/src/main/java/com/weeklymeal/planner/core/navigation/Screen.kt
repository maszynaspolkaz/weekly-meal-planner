package com.weeklymeal.planner.core.navigation

import java.time.LocalDate

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data object Game : Screen
    data class GameOver(val weekStart: LocalDate, val revision: Long) : Screen
    data class Recipes(val targetDate: LocalDate? = null) : Screen
    data class RecipeDetail(val recipeId: String, val date: LocalDate? = null) : Screen
    data class ShoppingList(val weekStart: LocalDate) : Screen
    data class PantryCheck(val weekStart: LocalDate) : Screen
    data object Settings : Screen
}

sealed interface AppEvent {
    data class Navigate(val screen: Screen) : AppEvent
    data object Back : AppEvent
    data object Menu : AppEvent
    data object Again : AppEvent
    data object Saved : AppEvent
    data object Close : AppEvent
}
