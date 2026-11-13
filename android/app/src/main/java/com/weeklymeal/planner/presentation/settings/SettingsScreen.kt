package com.weeklymeal.planner.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.WeekStart

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onSelect: (WeekStart) -> Unit, onSave: () -> Unit, onSaveAndLeave: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, onRetry: () -> Unit) {
    AppPage {
        ScreenHeader(R.string.settings_title, onBack)
        InlineStatus(state.plan, onRetry)
        ProduceCard {
            Text(stringResource(R.string.week_start_label), style = MaterialTheme.typography.titleMedium)
            WeekStart.entries.forEach { option ->
                val selected = state.selected == option
                val description = stringResource(if (selected) R.string.selected_state else R.string.unselected_state)
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected, enabled = !state.plan.saving, role = Role.RadioButton, onClick = { onSelect(option) }).semantics { stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected, onClick = null, enabled = !state.plan.saving, colors = RadioButtonDefaults.colors(selectedColor = Action), modifier = Modifier.size(48.dp))
                    Text(stringResource(if (option == WeekStart.MONDAY) R.string.monday else R.string.sunday))
                }
            }
            Text(stringResource(R.string.settings_help), color = TextSecondary)
        }
        ProduceCard {
            Image(painterResource(R.drawable.icon_pause), null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.privacy_message), color = TextSecondary)
        }
        if (state.savedFeedback) Text(stringResource(R.string.week_preference_saved), color = TextSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        PrimaryButton(R.string.save_settings, onSave, enabled = state.dirty && !state.plan.saving, busy = state.plan.saving)
    }
    if (state.discardDialog) AlertDialog(onDismissRequest = onKeepEditing,
        title = { Text(stringResource(R.string.settings_discard_title)) }, text = { Text(stringResource(R.string.settings_discard_message)) },
        confirmButton = { ActionText(R.string.save, onSaveAndLeave, enabled = !state.plan.saving) },
        dismissButton = { Column { ActionText(R.string.discard, onDiscard, enabled = !state.plan.saving); ActionText(R.string.keep_editing, onKeepEditing, enabled = !state.plan.saving) } })
}
