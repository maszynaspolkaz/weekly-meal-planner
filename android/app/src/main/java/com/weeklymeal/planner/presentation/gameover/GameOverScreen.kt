package com.weeklymeal.planner.presentation.gameover

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*

@Composable
fun GameOverScreen(state: GameOverUiState, onBack: () -> Unit, onShopping: () -> Unit, onPantry: () -> Unit, onAgain: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val plan = state.plan
    val summary = plan.summary
    AppPage(3) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ActionText(R.string.back, onBack)
            ActionText(R.string.menu, onMenu)
        }
        InlineStatus(plan, onRetry)
        if (summary.dinners.isEmpty() && !plan.loading) {
            EmptyState(R.string.empty_week_title, R.string.empty_week_body, R.string.plan_dinners, onAgain)
        } else {
            Text(stringResource(R.string.ready_eyebrow), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(104.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.ready_title), style = MaterialTheme.typography.headlineMedium)
                    Text(weekLabel(summary.week), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            StatRow(listOf(summary.dinners.size to R.string.dinners_planned, summary.requirements.size to R.string.ingredients_combined, summary.remaining to R.string.items_to_buy))
            ProduceCard {
                Text(stringResource(R.string.summary_portions, summary.dinners.size, summary.portions, summary.requirements.map { it.ingredient.section }.distinct().size), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(when {
                    summary.requirements.isNotEmpty() && summary.atHome == summary.requirements.size -> R.string.ready_at_home
                    summary.remaining == 0 -> R.string.shopping_complete
                    else -> R.string.ready_message
                }), style = MaterialTheme.typography.bodyLarge, color = TextSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            PrimaryButton(R.string.view_shopping_list, onShopping, enabled = !plan.loading && !plan.error)
            Text(stringResource(R.string.ready_hint), color = TextSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton(R.string.pantry_check, onPantry, Modifier.weight(1f))
                SecondaryButton(R.string.play_again, onAgain, Modifier.weight(1f))
            }
        }
    }
}
