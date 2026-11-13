package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.domain.model.IngredientRequirement

@Composable
fun IngredientChecklistRow(item: IngredientRequirement, checked: Boolean, atHomeMode: Boolean, expanded: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit, onDetails: () -> Unit) {
    val description = stringResource(if (atHomeMode) { if (checked) R.string.at_home_state else R.string.not_at_home } else { if (checked) R.string.bought else R.string.not_bought })
    ProduceCard {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = onToggle).semantics { stateDescription = description }) {
            Checkbox(checked, onCheckedChange = null, enabled = enabled, modifier = Modifier.size(48.dp), colors = CheckboxDefaults.colors(checkedColor = Action, checkmarkColor = Surface))
            Column(Modifier.weight(1f).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.ingredient.name.localized(), style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Text(quantityLabel(item.amount, item.ingredient.measurement), color = TextSecondary)
                if (checked) Text(stringResource(if (atHomeMode) R.string.at_home else R.string.bought), style = MaterialTheme.typography.labelSmall, color = Action)
            }
        }
        ActionText(if (expanded) R.string.hide_details else R.string.details, onDetails)
        if (expanded) {
            item.contributions.forEach { contribution ->
                Text(stringResource(R.string.contribution, dayLabel(contribution.date), contribution.title.localized(), contributionLabel(contribution.numerator, item.ingredient.measurement)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Text(stringResource(R.string.rounding_note), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
