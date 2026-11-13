package com.weeklymeal.planner.core.di

import android.content.Context
import com.weeklymeal.planner.data.local.PlannerJsonCodec
import com.weeklymeal.planner.data.local.PlannerPreferences
import com.weeklymeal.planner.data.repository.PlannerRepositoryImpl
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.model.Label
import com.weeklymeal.planner.core.text.labelResource
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.domain.usecase.ReconcileRequirementChecks
import kotlinx.coroutines.Dispatchers

object ServiceLocator {
    private var planner: PlannerRepository? = null
    private var appContext: Context? = null
    val aggregate = AggregateIngredients()
    val repository: PlannerRepository get() = requireNotNull(planner)
    val factory: AppViewModelFactory by lazy { AppViewModelFactory(repository, aggregate) }
    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (planner == null) planner = PlannerRepositoryImpl(PlannerPreferences(context.applicationContext), PlannerJsonCodec(), ReconcileRequirementChecks(aggregate), Dispatchers.IO)
    }
    fun resolve(label: Label): String = if (label.resource) requireNotNull(appContext).getString(labelResource(label.value)) else label.value
}
