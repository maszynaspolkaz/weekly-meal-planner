package com.weeklymeal.planner.presentation.pantry

import com.weeklymeal.planner.presentation.app.PlanUi

data class PantryCheckUiState(val plan: PlanUi = PlanUi(), val expanded: Set<String> = emptySet(), val clearDialog: Boolean = false)
