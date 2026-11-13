package com.weeklymeal.planner.presentation.app

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklymeal.planner.core.di.ServiceLocator
import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.presentation.splash.SplashRoute
import com.weeklymeal.planner.presentation.menu.*
import com.weeklymeal.planner.presentation.game.*
import com.weeklymeal.planner.presentation.gameover.*
import com.weeklymeal.planner.presentation.recipes.*
import com.weeklymeal.planner.presentation.recipedetail.*
import com.weeklymeal.planner.presentation.shopping.*
import com.weeklymeal.planner.presentation.pantry.*
import com.weeklymeal.planner.presentation.settings.*

@Composable
private fun ScreenEvents(model: PlannerViewModel, onEvent: (AppEvent) -> Unit) {
    val latest by rememberUpdatedState(onEvent)
    LaunchedEffect(Unit) { model.events.collect { latest(it) } }
}

@Composable
fun AppRoot() {
    val factory = ServiceLocator.factory
    val app: AppViewModel = viewModel(factory = factory)
    val menu: MenuViewModel = viewModel(factory = factory)
    val game: GameViewModel = viewModel(factory = factory)
    val result: GameOverViewModel = viewModel(factory = factory)
    val recipes: RecipesViewModel = viewModel(factory = factory)
    val detail: RecipeDetailViewModel = viewModel(factory = factory)
    val shopping: ShoppingListViewModel = viewModel(factory = factory)
    val pantry: PantryCheckViewModel = viewModel(factory = factory)
    val settings: SettingsViewModel = viewModel(factory = factory)
    val screen by app.screen.collectAsStateWithLifecycle()
    val menuState by menu.state.collectAsStateWithLifecycle()
    val gameState by game.state.collectAsStateWithLifecycle()
    val resultState by result.state.collectAsStateWithLifecycle()
    val recipesState by recipes.state.collectAsStateWithLifecycle()
    val detailState by detail.state.collectAsStateWithLifecycle()
    val shoppingState by shopping.state.collectAsStateWithLifecycle()
    val pantryState by pantry.state.collectAsStateWithLifecycle()
    val settingsState by settings.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val view = LocalView.current
    val savedScreens = rememberSaveableStateHolder()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { if (screen == Screen.Menu) menu.resume() }
    ScreenEvents(menu, app::handle)
    ScreenEvents(game, app::handle)
    ScreenEvents(result, app::handle)
    ScreenEvents(recipes, app::handle)
    ScreenEvents(detail, app::handle)
    ScreenEvents(shopping, app::handle)
    ScreenEvents(pantry, app::handle)
    ScreenEvents(settings, app::handle)
    LaunchedEffect(Unit) {
        app.events.collect { event ->
            when (event) {
                AppEvent.Close -> activity?.finish()
                AppEvent.Saved -> view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                else -> Unit
            }
        }
    }
    LaunchedEffect(screen) {
        when (val route = screen) {
            is Screen.GameOver -> result.enter(route.weekStart)
            is Screen.Recipes -> recipes.enter(route.targetDate)
            is Screen.RecipeDetail -> detail.enter(route.recipeId, route.date)
            is Screen.ShoppingList -> shopping.enter(route.weekStart)
            is Screen.PantryCheck -> pantry.enter(route.weekStart)
            else -> Unit
        }
    }
    BackHandler {
        when (screen) {
            Screen.Menu -> if (menuState.confirmReset) menu.resetDialog(false) else app.back()
            Screen.Game -> game.back()
            is Screen.Recipes -> recipes.back()
            is Screen.RecipeDetail -> detail.back()
            is Screen.ShoppingList -> shopping.back()
            is Screen.PantryCheck -> pantry.back()
            Screen.Settings -> settings.back()
            else -> app.back()
        }
    }
    AnimatedContent(targetState = screen, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }, label = "screen") { route ->
        savedScreens.SaveableStateProvider(route.toString()) {
        when (route) {
            Screen.Splash -> SplashRoute(app::splashFinished)
            Screen.Menu -> MenuScreen(menuState, menu::play,
                { menu.navigate(Screen.Recipes()) },
                { menu.navigate(Screen.ShoppingList(menuState.plan.summary.week)) },
                { menu.navigate(Screen.Settings) }, menu::reload, { menu.resetDialog(true) }, menu::reset, { menu.resetDialog(false) })
            Screen.Game -> {
                DisposableEffect(Unit) { game.resume(); onDispose { game.pause() } }
                LifecycleEventEffect(Lifecycle.Event.ON_STOP) { game.pause() }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { game.resume() }
                GameScreen(gameState, game::action, game.scrollEvents)
            }
            is Screen.GameOver -> GameOverScreen(resultState, result::back,
                { result.navigate(Screen.ShoppingList(resultState.plan.summary.week)) },
                { result.navigate(Screen.PantryCheck(resultState.plan.summary.week)) }, result::again, result::menu, result::reload)
            is Screen.Recipes -> RecipesScreen(recipesState, recipes::back, { recipes.edit() }, recipes::query, recipes::category, recipes::sort, recipes::clearFilters,
                { recipes.navigate(Screen.RecipeDetail(it.id)) }, recipes::schedule, { recipes.chooseDay(it) },
                { recipesState.pendingDate?.let { recipes.chooseDay(it, true) } }, recipes::dismissPicker, recipes::editorAction, recipes::delete, { recipes.deleteAsk(null) }, recipes::retry)
            is Screen.RecipeDetail -> RecipeDetailScreen(detailState, detail::back,
                { detail.navigate(Screen.Recipes()) }, detail::servings, detail::add, { detail.choose(it) },
                { detailState.pendingDate?.let { detail.choose(it, true) } }, detail::dismiss,
                { recipes.edit(it); detail.navigate(Screen.Recipes()) }, { detail.navigate(Screen.RecipeDetail(it.id)) }, detail::deleteAsk, detail::delete, detail::retry)
            is Screen.ShoppingList -> ShoppingListScreen(shoppingState, shopping::back,
                { shopping.navigate(Screen.PantryCheck(shoppingState.plan.summary.week)) }, shopping::planDinners,
                shopping::toggle, shopping::details, shopping::hide, { shopping.done() }, { shopping.done(true) }, shopping::resetAsk, shopping::reset, shopping::dismiss, shopping::retry)
            is Screen.PantryCheck -> PantryCheckScreen(pantryState, pantry::back,
                { pantry.navigate(Screen.ShoppingList(pantryState.plan.summary.week)) }, pantry::planDinners,
                pantry::toggle, pantry::details, pantry::clearAsk, pantry::clear, pantry::dismiss, pantry::retry)
            Screen.Settings -> SettingsScreen(settingsState, settings::back, settings::select, { settings.save() }, { settings.save(true) }, settings::discard, settings::keepEditing, settings::retry)
        }
        }
    }
}
