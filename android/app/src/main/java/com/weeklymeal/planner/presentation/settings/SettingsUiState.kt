package com.weeklymeal.planner.presentation.settings

import com.weeklymeal.planner.domain.model.WeekStart
import com.weeklymeal.planner.presentation.app.PlanUi

data class SettingsUiState(
    val plan: PlanUi = PlanUi(),
    val stored: WeekStart = WeekStart.MONDAY,
    val selected: WeekStart = WeekStart.MONDAY,
    val discardDialog: Boolean = false,
    val savedFeedback: Boolean = false
) { val dirty: Boolean get() = stored != selected }
