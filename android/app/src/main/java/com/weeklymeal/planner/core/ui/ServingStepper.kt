package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R

@Composable
fun ServingStepper(value: Int, onChange: (Int) -> Unit, enabled: Boolean = true) {
    val description = stringResource(R.string.serving_state, value)
    Column(Modifier.fillMaxWidth().semantics { stateDescription = description }) {
        Text(stringResource(R.string.servings), style = MaterialTheme.typography.labelSmall)
        Text(stringResource(R.string.serving_value, value), style = MaterialTheme.typography.titleMedium)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ActionText(R.string.decrease, { onChange(value - 1) }, enabled = enabled && value > 1)
            ActionText(R.string.increase, { onChange(value + 1) }, enabled = enabled && value < 12)
        }
    }
}
