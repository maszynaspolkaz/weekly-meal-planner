package com.weeklymeal.planner.presentation.app

import com.weeklymeal.planner.core.navigation.Screen

data class AppUiState(val current: Screen = Screen.Splash, val backStack: List<Screen> = emptyList())
