package com.weeklymeal.planner.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.presentation.recipes.*

@Composable
fun RecipeEditorDialog(state: RecipesUiState, onAction: (EditorAction) -> Unit, onRetry: () -> Unit) {
    val recipe = state.editor ?: return
    val catalog = state.plan.data?.ingredientCatalog.orEmpty() + state.extraIngredients
    val focus = remember { FocusRequester() }
    LaunchedEffect(state.invalid) { if (state.invalid) focus.requestFocus() }
    Dialog(onDismissRequest = { onAction(EditorAction.Cancel) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.safeDrawingPadding().imePadding().padding(12.dp).widthIn(max = 560.dp).fillMaxWidth().fillMaxHeight(0.94f), shape = RoundedCornerShape(18.dp), color = Background, border = BorderStroke(1.dp, BorderSage)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (state.plan.data?.recipes?.any { it.id == recipe.id } == true) R.string.edit_recipe else R.string.new_recipe), style = MaterialTheme.typography.titleLarge)
                InlineStatus(state.plan.copy(loading = false), onRetry)
                if (state.invalid) Text(stringResource(R.string.validation_summary), color = Error, modifier = Modifier.focusRequester(focus).focusable().semantics { liveRegion = LiveRegionMode.Polite })
                OutlinedTextField(value = recipe.title.localized(), onValueChange = { onAction(EditorAction.Title(it)) }, label = { Text(stringResource(R.string.recipe_title_label)) }, modifier = Modifier.fillMaxWidth(), isError = state.invalid && recipe.title.value.isBlank(), supportingText = {
                    if (state.invalid && recipe.title.value.isBlank()) Text(stringResource(R.string.title_error))
                })
                Text(stringResource(R.string.category_label), style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(Category.entries.toList()) { category ->
                    val description = stringResource(if (category == recipe.category) R.string.selected_state else R.string.unselected_state)
                    FilterChip(category == recipe.category, { onAction(EditorAction.SetCategory(category)) }, label = { Text(stringResource(categoryResource(category))) }, modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = description })
                } }
                ServingStepper(recipe.baseServings, { onAction(EditorAction.Servings(it)) }, !state.plan.saving)
                Text(stringResource(R.string.duration_label), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.duration_value, recipe.minutes), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ActionText(R.string.less, { onAction(EditorAction.Duration(recipe.minutes - 5)) }, enabled = recipe.minutes > 5)
                    ActionText(R.string.more, { onAction(EditorAction.Duration(recipe.minutes + 5)) }, enabled = recipe.minutes < 180)
                }
                Text(stringResource(R.string.illustration_label), style = MaterialTheme.typography.titleMedium)
                listOf("sprite_1", "sprite_2", "sprite_3", "sprite_4", "sprite_5").chunked(3).forEach { images ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        images.forEach { image ->
                            val description = stringResource(if (image == recipe.imageKey) R.string.selected_state else R.string.unselected_state)
                            OutlinedButton({ onAction(EditorAction.Image(image)) }, modifier = Modifier.weight(1f).heightIn(min = 72.dp).semantics { stateDescription = description },
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (image == recipe.imageKey) Secondary else Surface)) {
                                Image(recipePainter(image), stringResource(R.string.illustration_option, image.takeLast(1).toInt()), Modifier.size(44.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                Text(stringResource(R.string.ingredients), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.ingredient_limits), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (state.invalid && recipe.ingredients.isEmpty()) Text(stringResource(R.string.ingredients_error), color = Error)
                recipe.ingredients.forEach { line ->
                    val definition = catalog.find { it.id == line.ingredientId }
                    if (definition != null) ProduceCard {
                        Text(definition.name.localized(), style = MaterialTheme.typography.titleMedium)
                        Text(quantityLabel(line.amount, definition.measurement, line.ingredientId in state.largeUnits), color = TextSecondary)
                        if (definition.measurement != MeasurementType.COUNT) ActionText(if (definition.measurement == MeasurementType.MASS) {
                            if (line.ingredientId in state.largeUnits) R.string.use_grams else R.string.use_kilograms
                        } else { if (line.ingredientId in state.largeUnits) R.string.use_millilitres else R.string.use_litres }, { onAction(EditorAction.Unit(line.ingredientId)) })
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ActionText(R.string.less, { onAction(EditorAction.Amount(line.ingredientId, -1)) }, enabled = line.amount > 1)
                            ActionText(R.string.more, { onAction(EditorAction.Amount(line.ingredientId, 1)) }, enabled = line.amount < if (definition.measurement == MeasurementType.COUNT) 1000L else 100000L)
                        }
                        ActionText(R.string.remove_ingredient, { onAction(EditorAction.RemoveIngredient(line.ingredientId)) })
                    }
                }
                SecondaryButton(R.string.add_ingredient, { onAction(EditorAction.OpenIngredients) }, enabled = recipe.ingredients.size < 40)
                Text(stringResource(R.string.steps), style = MaterialTheme.typography.titleMedium)
                recipe.steps.forEachIndexed { index, step ->
                    OutlinedTextField(step.localized(), { onAction(EditorAction.Step(index, it)) }, label = { Text(stringResource(R.string.step_label, index + 1)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, isError = state.invalid && step.value.isBlank(), supportingText = {
                        if (state.invalid && step.value.isBlank()) Text(stringResource(R.string.step_error))
                    })
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ActionText(R.string.move_up, { onAction(EditorAction.MoveStep(index, -1)) }, enabled = index > 0)
                        ActionText(R.string.move_down, { onAction(EditorAction.MoveStep(index, 1)) }, enabled = index < recipe.steps.lastIndex)
                    }
                    ActionText(R.string.remove_step, { onAction(EditorAction.RemoveStep(index)) })
                }
                SecondaryButton(R.string.add_step, { onAction(EditorAction.AddStep) }, enabled = recipe.steps.size < 20)
                PrimaryButton(R.string.save_recipe, { onAction(EditorAction.Save) }, enabled = !state.plan.saving, busy = state.plan.saving)
                SecondaryButton(R.string.cancel, { onAction(EditorAction.Cancel) }, enabled = !state.plan.saving)
            }
        }
    }
    if (state.picker) IngredientPickerDialog(state, onAction)
    if (state.discardDialog) ConfirmationDialog(R.string.discard_recipe_title, stringResource(R.string.discard_recipe_message), R.string.discard, { onAction(EditorAction.Discard) }, { onAction(EditorAction.KeepEditing) }, R.string.keep_editing)
}

@Composable
fun IngredientPickerDialog(state: RecipesUiState, onAction: (EditorAction) -> Unit) {
    val catalog = state.plan.data?.ingredientCatalog.orEmpty() + state.extraIngredients
    val draft = state.ingredientDraft
    val sorter = labelSorter()
    Dialog(onDismissRequest = { onAction(EditorAction.CloseIngredients) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.safeDrawingPadding().imePadding().padding(12.dp).widthIn(max = 560.dp).fillMaxWidth().fillMaxHeight(0.85f), shape = RoundedCornerShape(18.dp), color = Background) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (draft == null) R.string.add_ingredient else R.string.create_ingredient), style = MaterialTheme.typography.titleLarge)
                if (draft == null) {
                    OutlinedTextField(state.ingredientQuery, { onAction(EditorAction.IngredientQuery(it)) }, label = { Text(stringResource(R.string.search_ingredients)) }, modifier = Modifier.fillMaxWidth())
                    SecondaryButton(R.string.create_ingredient, { onAction(EditorAction.CreateIngredient) })
                    catalog.filter { sorter(it.name).contains(state.ingredientQuery.trim(), true) }.sortedBy { sorter(it.name) }.forEach { ingredient ->
                        OutlinedButton({ onAction(EditorAction.AddIngredient(ingredient)) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = state.editor?.ingredients?.any { it.ingredientId == ingredient.id } != true) {
                            Text(ingredient.name.localized())
                        }
                    }
                } else {
                    OutlinedTextField(draft.name, { onAction(EditorAction.NewIngredient(draft.copy(name = it.take(40)))) }, label = { Text(stringResource(R.string.ingredient_name_label)) }, modifier = Modifier.fillMaxWidth(), isError = state.ingredientError)
                    if (state.ingredientError) Text(stringResource(R.string.ingredient_error), color = Error)
                    Text(stringResource(R.string.measurement_label), style = MaterialTheme.typography.titleMedium)
                    MeasurementType.entries.forEach { measurement ->
                        OutlinedButton({ onAction(EditorAction.NewIngredient(draft.copy(measurement = measurement))) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (measurement == draft.measurement) Secondary else Surface)) {
                            Text(stringResource(when (measurement) { MeasurementType.MASS -> R.string.mass; MeasurementType.VOLUME -> R.string.volume; MeasurementType.COUNT -> R.string.count }))
                        }
                    }
                    Text(stringResource(R.string.section_label), style = MaterialTheme.typography.titleMedium)
                    StoreSection.entries.forEach { section ->
                        OutlinedButton({ onAction(EditorAction.NewIngredient(draft.copy(section = section))) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (section == draft.section) Secondary else Surface)) {
                            Text(stringResource(sectionResource(section)))
                        }
                    }
                    PrimaryButton(R.string.add_ingredient, { onAction(EditorAction.SaveIngredient) })
                }
                ActionText(R.string.cancel, { onAction(EditorAction.CloseIngredients) })
            }
        }
    }
}
