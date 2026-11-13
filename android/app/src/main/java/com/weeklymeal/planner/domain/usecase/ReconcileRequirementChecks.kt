package com.weeklymeal.planner.domain.usecase

import com.weeklymeal.planner.domain.model.*
import java.time.LocalDate

class ReconcileRequirementChecks(private val aggregate: AggregateIngredients) {
    operator fun invoke(data: PlannerData): PlannerData {
        val checks = data.checks.mapValues { (key, saved) ->
            val week = LocalDate.parse(key.substringAfter(':'))
            val requirements = aggregate.summary(data, week).requirements.associate { it.ingredient.id to it.amount }
            saved.filter { (id, check) -> requirements[id] == check.amount }
        }.filterValues { it.isNotEmpty() }
        return data.copy(checks = checks)
    }
}
