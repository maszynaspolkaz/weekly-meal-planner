package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.presentation.app.PlanUi

@Composable
fun AppBackdrop(variant: Int = 1, content: @Composable BoxScope.() -> Unit) {
    val colors = when (variant) {
        0 -> listOf(BackgroundTint.copy(alpha = 0.90f), Secondary.copy(alpha = 0.97f))
        2 -> listOf(Background.copy(alpha = 0.96f), BackgroundTint.copy(alpha = 0.94f))
        3 -> listOf(Secondary.copy(alpha = 0.97f), BackgroundTint.copy(alpha = 0.94f), Background.copy(alpha = 0.98f))
        else -> listOf(Background.copy(alpha = 0.97f), BackgroundTint.copy(alpha = 0.89f), Background.copy(alpha = 0.96f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        content()
    }
}

@Composable
fun AppPage(variant: Int = 1, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    AppBackdrop(variant) {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().padding(horizontal = 20.dp)
            Column(if (scroll) modifier.verticalScroll(rememberScrollState()).padding(vertical = 12.dp) else modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
fun ProduceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BorderSage),
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.96f)), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun ScreenHeader(titleRes: Int, onBack: () -> Unit, actionRes: Int? = null, onAction: (() -> Unit)? = null) {
    Column {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = Action)) {
                Image(painterResource(R.drawable.icon_home), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.back))
            }
            if (actionRes != null && onAction != null) ActionText(actionRes, onAction)
        }
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
    }
}

@Composable
fun InlineStatus(plan: PlanUi, onRetry: () -> Unit, onDiscard: (() -> Unit)? = null) {
    val description = stringResource(when { plan.loading -> R.string.loading; plan.saving -> R.string.saving; plan.error -> R.string.unsaved_changes; else -> R.string.saved })
    when {
        plan.loading || plan.saving -> Row(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite; stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Action, strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(if (plan.loading) R.string.loading else R.string.saving), color = TextSecondary)
        }
        plan.error -> ProduceCard {
            Text(stringResource(if (plan.data == null) R.string.storage_error else R.string.unsaved_changes), color = Error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            ActionText(R.string.retry, onRetry)
            if (onDiscard != null) ActionText(R.string.discard_last_change, onDiscard)
        }
        plan.saved -> Text(stringResource(R.string.saved), color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    if (plan.checksChanged) Text(stringResource(R.string.checks_changed), color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
fun EmptyState(titleRes: Int, bodyRes: Int, actionRes: Int, onAction: () -> Unit) {
    ProduceCard {
        Image(painterResource(R.drawable.logo), null, Modifier.size(96.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
        Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(bodyRes), color = TextSecondary)
        SecondaryButton(actionRes, onAction)
    }
}
