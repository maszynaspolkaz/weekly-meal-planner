package com.weeklymeal.planner.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.presentation.app.AppViewModel
import com.weeklymeal.planner.presentation.splash.SplashViewModel
import com.weeklymeal.planner.presentation.menu.MenuViewModel
import com.weeklymeal.planner.presentation.game.GameViewModel
import com.weeklymeal.planner.presentation.gameover.GameOverViewModel
import com.weeklymeal.planner.presentation.recipes.RecipesViewModel
import com.weeklymeal.planner.presentation.recipedetail.RecipeDetailViewModel
import com.weeklymeal.planner.presentation.shopping.ShoppingListViewModel
import com.weeklymeal.planner.presentation.pantry.PantryCheckViewModel
import com.weeklymeal.planner.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val repository: PlannerRepository, private val aggregate: AggregateIngredients) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val model: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel()
            SplashViewModel::class.java -> SplashViewModel(repository)
            MenuViewModel::class.java -> MenuViewModel(repository, aggregate)
            GameViewModel::class.java -> GameViewModel(repository, aggregate)
            GameOverViewModel::class.java -> GameOverViewModel(repository, aggregate)
            RecipesViewModel::class.java -> RecipesViewModel(repository, aggregate, ServiceLocator::resolve)
            RecipeDetailViewModel::class.java -> RecipeDetailViewModel(repository, aggregate)
            ShoppingListViewModel::class.java -> ShoppingListViewModel(repository, aggregate)
            PantryCheckViewModel::class.java -> PantryCheckViewModel(repository, aggregate)
            SettingsViewModel::class.java -> SettingsViewModel(repository, aggregate)
            else -> throw IllegalArgumentException(modelClass.name)
        }
        return modelClass.cast(model) ?: throw IllegalArgumentException(modelClass.name)
    }
}
