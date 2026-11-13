package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.domain.model.Recipe

@Composable
fun recipePainter(key: String): Painter = when (key) {
    "sprite_2" -> painterResource(R.drawable.sprite_2)
    "sprite_3" -> painterResource(R.drawable.sprite_3)
    "sprite_4" -> painterResource(R.drawable.sprite_4)
    "sprite_5" -> painterResource(R.drawable.sprite_5)
    else -> painterResource(R.drawable.sprite_1)
}

@Composable
fun RecipeImage(recipe: Recipe, size: Dp = 72.dp, modifier: Modifier = Modifier) {
    Image(recipePainter(recipe.imageKey), recipe.title.localized(), modifier.size(size), contentScale = ContentScale.Fit)
}

@Composable
fun RecipeCard(recipe: Recipe, onView: () -> Unit, onAddToDay: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    ProduceCard(modifier) {
        if (compact) {
            RecipeImage(recipe, 44.dp)
            Text(recipe.title.localized(), style = MaterialTheme.typography.bodyMedium)
            ActionText(R.string.add_to_day, onAddToDay)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RecipeImage(recipe, 80.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(recipe.title.localized(), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(categoryResource(recipe.category)), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(stringResource(R.string.recipe_meta, recipe.baseServings, recipe.minutes), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            ActionText(R.string.view_recipe, onView)
            SecondaryButton(R.string.add_to_day, onAddToDay)
        }
    }
}
