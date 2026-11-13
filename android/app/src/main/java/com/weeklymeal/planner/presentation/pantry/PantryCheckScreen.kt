package com.weeklymeal.planner.presentation.pantry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.StoreSection

@Composable
fun PantryCheckScreen(state: PantryCheckUiState, onBack: () -> Unit, onShopping: () -> Unit, onPlan: () -> Unit, onToggle: (String, Boolean) -> Unit, onDetails: (String) -> Unit, onClearAsk: () -> Unit, onClear: () -> Unit, onDismiss: () -> Unit, onRetry: () -> Unit) {
    val plan = state.plan
    val summary = plan.summary
    val sortLabel = labelSorter()
    AppPage(2) {
        ScreenHeader(R.string.pantry_title, onBack)
        Text(weekLabel(summary.week), color = TextSecondary)
        InlineStatus(plan, onRetry)
        if (summary.dinners.isEmpty() && !plan.loading) {
            EmptyState(R.string.pantry_empty_title, R.string.pantry_empty_body, R.string.plan_dinners, onPlan)
        } else {
            Image(painterResource(R.drawable.sprite_5), null, Modifier.size(104.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.pantry_explanation), style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text(stringResource(R.string.pantry_excluded_count, summary.atHome), style = MaterialTheme.typography.bodyMedium, color = Action, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            StoreSection.entries.forEach { section ->
                val items = summary.requirements.filter { it.ingredient.section == section }.sortedBy { sortLabel(it.ingredient.name) }
                if (items.isNotEmpty()) {
                    Text(stringResource(sectionResource(section)), style = MaterialTheme.typography.titleMedium)
                    items.forEach { item ->
                        IngredientChecklistRow(item, summary.checks[item.ingredient.id]?.atHome == true, true, item.ingredient.id in state.expanded, !plan.saving,
                            { onToggle(item.ingredient.id, it) }, { onDetails(item.ingredient.id) })
                    }
                }
            }
            PrimaryButton(R.string.view_shopping_list, onShopping, enabled = !plan.saving && !plan.error)
            ActionText(R.string.clear_at_home, onClearAsk, enabled = summary.atHome > 0 && !plan.saving)
        }
    }
    if (state.clearDialog) ConfirmationDialog(R.string.clear_home_title, stringResource(R.string.clear_home_message), R.string.clear_checks, onClear, onDismiss)
}
