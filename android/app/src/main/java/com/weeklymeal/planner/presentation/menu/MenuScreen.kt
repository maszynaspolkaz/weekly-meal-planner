package com.weeklymeal.planner.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onRecipes: () -> Unit, onShopping: () -> Unit, onSettings: () -> Unit, onRetry: () -> Unit, onResetAsk: () -> Unit, onReset: () -> Unit, onDismiss: () -> Unit) {
    val plan = state.plan
    AppPage {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.eyebrow), style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
            ActionText(R.string.settings, onSettings, enabled = plan.data != null)
        }
        Text(stringResource(R.string.menu_headline), style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        ProduceCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.menu_hero), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    if (plan.data != null) Text(weekLabel(plan.summary.week), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(128.dp), contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("sprite_1", "sprite_2", "sprite_3", "sprite_4", "sprite_5").forEach {
                    Image(recipePainter(it), null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
                }
            }
        }
        StatRow(listOf(plan.summary.dinners.size to R.string.dinners_planned, (plan.data?.recipes?.size ?: 0) to R.string.saved_recipes, plan.summary.remaining to R.string.items_to_shop))
        InlineStatus(plan, onRetry)
        Text(stringResource(R.string.menu_hint), style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        PrimaryButton(R.string.play, onPlay, enabled = plan.data != null && !plan.error && !plan.loading)
        SecondaryButton(R.string.recipes, onRecipes, enabled = plan.data != null)
        SecondaryButton(R.string.shopping_list, onShopping, enabled = plan.data != null)
        if (plan.error && plan.data == null) SecondaryButton(R.string.reset_local_data, onResetAsk)
        else ProduceCard { Text(stringResource(R.string.menu_tip), style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
    }
    if (state.confirmReset) ConfirmationDialog(R.string.reset_title, stringResource(R.string.reset_message), R.string.reset_local_data, onReset, onDismiss)
}
