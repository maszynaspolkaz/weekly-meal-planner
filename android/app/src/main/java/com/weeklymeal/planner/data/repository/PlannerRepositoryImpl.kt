package com.weeklymeal.planner.data.repository

import com.weeklymeal.planner.data.local.PlannerJsonCodec
import com.weeklymeal.planner.data.local.PlannerPreferences
import com.weeklymeal.planner.data.sample.SampleData
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.ReconcileRequirementChecks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

class PlannerRepositoryImpl(
    private val preferences: PlannerPreferences,
    private val codec: PlannerJsonCodec,
    private val reconcile: ReconcileRequirementChecks,
    private val io: CoroutineDispatcher
) : PlannerRepository {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(StoreState())
    override val state = _state.asStateFlow()

    override suspend fun load(reset: Boolean) = withContext(io) {
        mutex.withLock {
            _state.update { it.copy(loading = true, error = false) }
            try {
                val payload = if (reset) null else preferences.read()
                val stored = if (payload == null) SampleData.create() else codec.decode(payload)
                val data = if (stored.followCurrentWeek) stored.copy(selectedWeek = weekContaining(LocalDate.now(), stored.weekStart)) else stored
                if (payload == null || stored.selectedWeek != data.selectedWeek) preferences.write(codec.encode(data))
                val checked = reconcile(data)
                require(checked.checks == data.checks)
                _state.update { it.copy(data = data, loading = false, error = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    override suspend fun update(transform: (PlannerData) -> PlannerData): PlannerData = withContext(io) {
        mutex.withLock {
            val current = requireNotNull(_state.value.data)
            val next = reconcile(transform(current).copy(revision = current.revision + 1))
            val payload = codec.encode(next)
            codec.decode(payload)
            preferences.write(payload)
            _state.update { it.copy(data = next, loading = false, error = false) }
            next
        }
    }
}
