package com.weeklymeal.planner.presentation.shopping

import com.weeklymeal.planner.presentation.app.PlanUi

data class ShoppingListUiState(
    val plan: PlanUi = PlanUi(),
    val hidePurchased: Boolean = false,
    val expanded: Set<String> = emptySet(),
    val resetDialog: Boolean = false,
    val finishDialog: Boolean = false
)
