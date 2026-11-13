package com.weeklymeal.planner.presentation.recipedetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.Recipe
import java.time.LocalDate

@Composable
fun RecipeDetailScreen(state: RecipeDetailUiState, onBack: () -> Unit, onRecipes: () -> Unit, onServings: (Int) -> Unit, onAdd: () -> Unit, onChooseDay: (LocalDate) -> Unit, onConfirmDay: () -> Unit, onDismiss: () -> Unit, onEdit: (Recipe) -> Unit, onOpenSaved: (Recipe) -> Unit, onDeleteAsk: () -> Unit, onDelete: () -> Unit, onRetry: () -> Unit) {
    val recipe = state.recipe
    AppPage(2) {
        ScreenHeader(R.string.recipe_detail, onBack)
        InlineStatus(state.plan, onRetry)
        if (recipe == null && !state.plan.loading) {
            EmptyState(R.string.missing_recipe, R.string.missing_recipe_body, R.string.back_to_recipes, onRecipes)
        } else if (recipe != null) {
            ProduceCard {
                RecipeImage(recipe, 160.dp, Modifier.align(Alignment.CenterHorizontally))
                Text(recipe.title.localized(), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(categoryResource(recipe.category)), style = MaterialTheme.typography.labelSmall, color = Action)
                Text(stringResource(R.string.recipe_meta, state.servings, recipe.minutes), color = TextSecondary)
                if (state.date != null) {
                    Text(stringResource(R.string.snapshot_note), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(dayLabel(state.date), color = TextSecondary)
                }
                ServingStepper(state.servings, onServings, !state.plan.saving)
            }
            ProduceCard {
                Text(stringResource(R.string.ingredients), style = MaterialTheme.typography.titleMedium)
                state.ingredients.forEach { ingredient ->
                    Text(stringResource(R.string.ingredient_line, ingredient.ingredient.name.localized(), quantityLabel(ingredient.amount, ingredient.ingredient.measurement)), style = MaterialTheme.typography.bodyLarge)
                }
            }
            ProduceCard {
                Text(stringResource(R.string.steps), style = MaterialTheme.typography.titleMedium)
                recipe.steps.forEachIndexed { index, step ->
                    Text(stringResource(R.string.step_text, index + 1, step.localized()), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            PrimaryButton(if (state.date == null) R.string.add_to_day else R.string.update_servings, onAdd, enabled = !state.plan.saving, busy = state.plan.saving)
            if (state.date == null) {
                SecondaryButton(R.string.edit_recipe, { onEdit(recipe) }, enabled = !state.plan.saving)
                ActionText(R.string.remove_recipe, onDeleteAsk, enabled = !state.plan.saving)
            } else if (state.plan.data?.recipes?.any { it.id == recipe.id } == true) SecondaryButton(R.string.open_saved_recipe, { onOpenSaved(recipe) }, enabled = !state.plan.saving)
        }
    }
    val data = state.plan.data
    if (state.dayPicker && state.pendingDate == null && data != null) DayPickerSheet(data, onChooseDay, onDismiss)
    if (state.pendingDate != null && recipe != null) ConfirmationDialog(R.string.replace_title,
        stringResource(R.string.replace_message, data?.dinners?.get(state.pendingDate)?.snapshot?.recipe?.title?.localized() ?: stringResource(R.string.unassigned), dayLabel(state.pendingDate), recipe.title.localized()),
        R.string.replace, onConfirmDay, onDismiss)
    if (state.deleteDialog) ConfirmationDialog(R.string.remove_recipe_title, stringResource(R.string.remove_recipe_message), R.string.remove, onDelete, onDismiss)
}
