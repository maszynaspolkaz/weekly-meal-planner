package com.weeklymeal.planner.domain

import com.weeklymeal.planner.core.navigation.Screen
import com.weeklymeal.planner.presentation.app.AppViewModel
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class NavigationTest {
    @Test
    fun splashIsExcludedAndBackReturnsToPreviousScreen() {
        val model = AppViewModel()
        model.splashFinished()
        assertEquals(Screen.Menu, model.screen.value)
        assertTrue(model.state.value.backStack.isEmpty())
        model.navigate(Screen.Game)
        model.navigate(Screen.Recipes())
        model.back()
        assertEquals(Screen.Game, model.screen.value)
        model.back()
        assertEquals(Screen.Menu, model.screen.value)
    }

    @Test
    fun replayDoesNotGrowPlanningResultHistory() {
        val model = AppViewModel()
        model.splashFinished()
        model.navigate(Screen.Game)
        repeat(10) {
            model.navigate(Screen.GameOver(LocalDate.of(2026, 11, 9), it.toLong()))
            model.again()
            assertEquals(listOf(Screen.Menu), model.state.value.backStack)
        }
        model.menu()
        assertEquals(Screen.Menu, model.screen.value)
        assertTrue(model.state.value.backStack.isEmpty())
    }
}
