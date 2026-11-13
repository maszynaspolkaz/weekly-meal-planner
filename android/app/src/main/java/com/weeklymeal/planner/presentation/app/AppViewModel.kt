package com.weeklymeal.planner.presentation.app

import androidx.lifecycle.ViewModel
import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.core.navigation.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = _screen.asStateFlow()
    private val channel = Channel<AppEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    fun splashFinished() {
        if (_screen.value == Screen.Splash) menu()
    }
    fun navigate(target: Screen) {
        if (_screen.value == target) return
        _state.update { it.copy(current = target, backStack = if (it.current == Screen.Splash) it.backStack else it.backStack + it.current) }
        _screen.value = target
    }
    fun back() {
        val state = _state.value
        if (state.current == Screen.Menu || state.current == Screen.Splash) {
            channel.trySend(AppEvent.Close)
        } else {
            val previous = state.backStack.lastOrNull() ?: Screen.Menu
            _state.update { it.copy(current = previous, backStack = it.backStack.dropLast(1)) }
            _screen.value = previous
        }
    }
    fun menu() {
        _state.update { it.copy(current = Screen.Menu, backStack = emptyList()) }
        _screen.value = Screen.Menu
    }
    fun again() {
        val index = _state.value.backStack.indexOfLast { it == Screen.Game }
        _state.update { it.copy(current = Screen.Game, backStack = if (index >= 0) it.backStack.take(index) else listOf(Screen.Menu)) }
        _screen.value = Screen.Game
    }
    fun handle(event: AppEvent) {
        when (event) {
            is AppEvent.Navigate -> navigate(event.screen)
            AppEvent.Back -> back()
            AppEvent.Menu -> menu()
            AppEvent.Again -> again()
            AppEvent.Close, AppEvent.Saved -> channel.trySend(event)
        }
    }
}
