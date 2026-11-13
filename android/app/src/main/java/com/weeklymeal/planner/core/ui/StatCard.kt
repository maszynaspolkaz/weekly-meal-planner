package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.Action
import com.weeklymeal.planner.core.theme.TextSecondary

@Composable
fun StatCard(value: Int, labelRes: Int, modifier: Modifier = Modifier) {
    ProduceCard(modifier) {
        Text(stringResource(R.string.number, value), style = MaterialTheme.typography.headlineMedium, color = Action)
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun StatRow(stats: List<Pair<Int, Int>>) {
    val visible = stats.filter { it.first > 0 }
    if (visible.size >= 2) {
        Row(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            visible.forEach { (value, label) -> StatCard(value, label, Modifier.weight(1f)) }
        }
    }
}
