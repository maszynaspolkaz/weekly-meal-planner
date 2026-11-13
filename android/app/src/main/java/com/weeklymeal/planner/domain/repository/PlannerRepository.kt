package com.weeklymeal.planner.domain.repository

import com.weeklymeal.planner.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface PlannerRepository {
    val state: StateFlow<StoreState>
    suspend fun load(reset: Boolean = false)
    suspend fun update(transform: (PlannerData) -> PlannerData): PlannerData
}
