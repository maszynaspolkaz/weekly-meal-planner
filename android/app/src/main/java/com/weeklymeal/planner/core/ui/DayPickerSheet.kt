package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.domain.model.PlannerData
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPickerSheet(data: PlannerData, onChooseDate: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().heightIn(max = 600.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.choose_day), style = MaterialTheme.typography.titleLarge)
            Text(weekLabel(data.selectedWeek), style = MaterialTheme.typography.bodyMedium)
            repeat(7) { index ->
                val date = data.selectedWeek.plusDays(index.toLong())
                val recipe = data.dinners[date]?.snapshot?.recipe
                OutlinedButton(onClick = { onChooseDate(date) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(stringResource(R.string.day_choice, dayLabel(date), recipe?.title?.localized() ?: stringResource(R.string.unassigned)))
                }
            }
            ActionText(R.string.cancel, onDismiss)
        }
    }
}
