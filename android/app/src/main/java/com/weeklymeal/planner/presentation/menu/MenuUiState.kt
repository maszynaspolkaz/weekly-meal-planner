package com.weeklymeal.planner.presentation.menu

import com.weeklymeal.planner.presentation.app.PlanUi

data class MenuUiState(val plan: PlanUi = PlanUi(), val confirmReset: Boolean = false)
