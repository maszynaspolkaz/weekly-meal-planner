package com.weeklymeal.planner.presentation.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.StoreSection

@Composable
fun ShoppingListScreen(state: ShoppingListUiState, onBack: () -> Unit, onPantry: () -> Unit, onPlan: () -> Unit, onToggle: (String, Boolean) -> Unit, onDetails: (String) -> Unit, onHide: () -> Unit, onDone: () -> Unit, onFinish: () -> Unit, onResetAsk: () -> Unit, onReset: () -> Unit, onDismiss: () -> Unit, onRetry: () -> Unit) {
    val plan = state.plan
    val summary = plan.summary
    val sortLabel = labelSorter()
    AppPage(2) {
        ScreenHeader(R.string.shopping_title, onBack, R.string.pantry_check, onPantry)
        Text(weekLabel(summary.week), color = TextSecondary)
        InlineStatus(plan, onRetry)
        if (summary.dinners.isEmpty() && !plan.loading) {
            EmptyState(R.string.shopping_empty_title, R.string.shopping_empty_body, R.string.plan_dinners, onPlan)
        } else {
            Text(stringResource(R.string.remaining_count, summary.remaining), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (summary.atHome > 0) {
                Text(stringResource(R.string.at_home_count, summary.atHome), color = TextSecondary)
                ActionText(R.string.review_pantry, onPantry)
            }
            val filterDescription = stringResource(if (state.hidePurchased) R.string.hide_bought else R.string.show_bought)
            ActionText(if (state.hidePurchased) R.string.show_bought else R.string.hide_bought, onHide, Modifier.semantics { stateDescription = filterDescription })
            if (summary.requirements.isNotEmpty() && summary.atHome == summary.requirements.size) {
                EmptyState(R.string.all_at_home, R.string.all_at_home_body, R.string.review_pantry, onPantry)
            } else {
                if (summary.remaining == 0 && summary.requirements.isNotEmpty()) Text(stringResource(R.string.shopping_complete), style = MaterialTheme.typography.titleMedium, color = Action)
                StoreSection.entries.forEach { section ->
                    val items = summary.requirements.filter {
                        it.ingredient.section == section && summary.checks[it.ingredient.id]?.atHome != true && (!state.hidePurchased || summary.checks[it.ingredient.id]?.purchased != true)
                    }.sortedBy { sortLabel(it.ingredient.name) }
                    if (items.isNotEmpty()) {
                        Text(stringResource(sectionResource(section)), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        items.forEach { item ->
                            IngredientChecklistRow(item, summary.checks[item.ingredient.id]?.purchased == true, false, item.ingredient.id in state.expanded, !plan.saving,
                                { onToggle(item.ingredient.id, it) }, { onDetails(item.ingredient.id) })
                        }
                    }
                }
            }
            PrimaryButton(R.string.done_shopping, onDone, enabled = !plan.saving && !plan.error && !plan.loading)
            ActionText(R.string.reset_bought, onResetAsk, enabled = summary.bought > 0 && !plan.saving)
        }
    }
    if (state.resetDialog) ConfirmationDialog(R.string.reset_bought_title, stringResource(R.string.reset_bought_message), R.string.clear_checks, onReset, onDismiss)
    if (state.finishDialog) ConfirmationDialog(R.string.finish_title, stringResource(R.string.finish_message, summary.remaining), R.string.finish_for_now, onFinish, onDismiss, R.string.keep_shopping)
}
