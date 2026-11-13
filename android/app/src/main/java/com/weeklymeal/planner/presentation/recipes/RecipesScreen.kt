package com.weeklymeal.planner.presentation.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.TextSecondary
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.*
import java.time.LocalDate

@Composable
fun RecipesScreen(state: RecipesUiState, onBack: () -> Unit, onNew: () -> Unit, onQuery: (String) -> Unit, onCategory: (Category?) -> Unit, onSort: (RecipeSort) -> Unit, onClear: () -> Unit, onView: (Recipe) -> Unit, onAdd: (Recipe) -> Unit, onChooseDay: (LocalDate) -> Unit, onConfirmDay: () -> Unit, onDismissPicker: () -> Unit, onEditorAction: (EditorAction) -> Unit, onDelete: () -> Unit, onDismissDelete: () -> Unit, onRetry: () -> Unit) {
    AppPage(2, scroll = false) {
        ScreenHeader(R.string.recipes_title, onBack, R.string.new_recipe, onNew)
        InlineStatus(state.plan, onRetry)
        if (state.targetDate != null) Text(stringResource(R.string.choose_recipe_day, dayLabel(state.targetDate)), color = TextSecondary)
        OutlinedTextField(state.query, onQuery, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_recipes)) }, singleLine = true)
        if (state.query.isNotEmpty()) ActionText(R.string.clear_query, { onQuery("") })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf<Category?>(null) + Category.entries) { category ->
                val selected = state.category == category
                val description = stringResource(if (selected) R.string.selected_state else R.string.unselected_state)
                FilterChip(selected, { onCategory(category) }, label = { Text(stringResource(category?.let(::categoryResource) ?: R.string.filter_all)) }, modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = description })
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RecipeSort.entries.toList()) { sort ->
                val description = stringResource(if (sort == state.sort) R.string.selected_state else R.string.unselected_state)
                FilterChip(sort == state.sort, { onSort(sort) }, label = { Text(stringResource(sortResource(sort))) }, modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = description })
            }
        }
        if (state.visible.isEmpty() && !state.plan.loading) {
            LazyColumn(Modifier.weight(1f)) { item {
                if (state.plan.data?.recipes?.isEmpty() == true) EmptyState(R.string.empty_recipes_title, R.string.empty_recipes_body, R.string.new_recipe, onNew)
                else EmptyState(R.string.no_matches, R.string.no_matches_body, R.string.clear_filters, onClear)
            } }
        } else LazyVerticalGrid(GridCells.Adaptive(280.dp), Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(state.visible, key = { it.id }) { recipe -> RecipeCard(recipe, { onView(recipe) }, { onAdd(recipe) }) }
        }
    }
    if (state.editor != null) RecipeEditorDialog(state, onEditorAction, onRetry)
    val data = state.plan.data
    if (state.dayRecipe != null && state.pendingDate == null && data != null) DayPickerSheet(data, onChooseDay, onDismissPicker)
    if (state.pendingDate != null && state.dayRecipe != null) ConfirmationDialog(R.string.replace_title,
        stringResource(R.string.replace_message, data?.dinners?.get(state.pendingDate)?.snapshot?.recipe?.title?.localized() ?: stringResource(R.string.unassigned), dayLabel(state.pendingDate), state.dayRecipe.title.localized()),
        R.string.replace, onConfirmDay, onDismissPicker)
    if (state.deleteCandidate != null) ConfirmationDialog(R.string.remove_recipe_title, stringResource(R.string.remove_recipe_message), R.string.remove, onDelete, onDismissDelete)
}
