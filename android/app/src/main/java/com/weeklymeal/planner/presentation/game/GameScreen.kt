package com.weeklymeal.planner.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.*
import com.weeklymeal.planner.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.math.roundToInt

private data class DragItem(val recipe: Recipe, val source: LocalDate?, val point: Offset)

@Composable
fun GameScreen(state: GameUiState, onAction: (GameAction) -> Unit, scrollEvents: Flow<Float>) {
    val plan = state.plan
    val data = plan.data
    val boardState = rememberLazyListState()
    val targets = remember { mutableStateMapOf<LocalDate, Rect>() }
    var boardBounds by remember { mutableStateOf(Rect.Zero) }
    val rootOrigin = Offset.Zero
    var drag by remember { mutableStateOf<DragItem?>(null) }
    val density = LocalDensity.current
    val threshold = with(density) { 48.dp.toPx() }
    val maxVelocity = with(density) { 240.dp.toPx() }
    val currentAction by rememberUpdatedState(onAction)
    LaunchedEffect(Unit) { scrollEvents.collect { boardState.dispatchRawDelta(it) } }
    fun updateDrag(point: Offset) {
        drag = drag?.copy(point = point)
        val velocity = when {
            point.y < boardBounds.top + threshold -> -maxVelocity * ((boardBounds.top + threshold - point.y) / threshold).coerceIn(0f, 1f)
            point.y > boardBounds.bottom - threshold -> maxVelocity * ((point.y - boardBounds.bottom + threshold) / threshold).coerceIn(0f, 1f)
            else -> 0f
        }
        currentAction(GameAction.Scroll(velocity))
    }
    fun stopDrag(commit: Boolean) {
        val active = drag
        if (commit && active != null) {
            val date = targets.entries.firstOrNull {
                !it.key.isBefore(plan.summary.week) && it.key.isBefore(plan.summary.week.plusDays(7)) && boardBounds.contains(active.point) && it.value.contains(active.point)
            }?.key
            if (date != null) currentAction(GameAction.Drop(active.recipe, date, active.source))
        }
        drag = null
        currentAction(GameAction.Scroll(0f))
    }
    @Composable
    fun dragModifier(recipe: Recipe, source: LocalDate?): Modifier {
        var origin by remember(recipe.id, source) { mutableStateOf(Offset.Zero) }
        val viewLabel = stringResource(R.string.view_recipe)
        return Modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(viewLabel) {
                currentAction(if (source == null) GameAction.ViewRecipe(recipe) else GameAction.ViewDinner(source))
                true
            })
        }.onGloballyPositioned { origin = it.positionInRoot() }.pointerInput(recipe, source, plan.saving, plan.error) {
            if (!plan.saving && !plan.error) detectDragGesturesAfterLongPress(
                onDragStart = { drag = DragItem(recipe, source, origin + it) },
                onDragEnd = { stopDrag(true) }, onDragCancel = { stopDrag(false) },
                onDrag = { change, amount -> change.consume(); drag?.let { updateDrag(it.point + amount) } }
            )
        }
    }
    @Composable
    fun dayCard(date: LocalDate) {
        DisposableEffect(date) { onDispose { targets.remove(date) } }
        val dinner = data?.dinners?.get(date)
        val selected = drag?.let { targets[date]?.contains(it.point) } == true
        val description = if (dinner == null) stringResource(R.string.empty_day_state, dayLabel(date)) else stringResource(R.string.filled_day_state, dayLabel(date), dinner.snapshot.recipe.title.localized(), dinner.servings)
        val modifier = Modifier.onGloballyPositioned { targets[date] = it.boundsInRoot() }.semantics { stateDescription = description }
        Card(modifier = modifier.then(if (dinner != null) dragModifier(dinner.snapshot.recipe, date) else Modifier).fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Action else BorderSage),
            colors = CardDefaults.cardColors(containerColor = if (selected) Secondary else Surface.copy(alpha = 0.96f)), elevation = CardDefaults.cardElevation(3.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text(dayLabel(date), style = MaterialTheme.typography.labelSmall, color = if (date == LocalDate.now()) Action else TextSecondary)
                if (date == state.lastChangedDay) Text(stringResource(R.string.saved), color = Action, style = MaterialTheme.typography.labelSmall, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                if (dinner == null) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.add_dinner), color = TextSecondary, modifier = Modifier.weight(1f))
                    ActionText(R.string.add, { onAction(GameAction.AddDate(date)) }, enabled = !plan.saving && !plan.error)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecipeImage(dinner.snapshot.recipe, 48.dp)
                        Column(Modifier.weight(1f)) {
                            Text(dinner.snapshot.recipe.title.localized(), style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.recipe_meta, dinner.servings, dinner.snapshot.recipe.minutes), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                    ActionText(R.string.edit, { onAction(GameAction.Focus(date)) }, enabled = !plan.saving && !plan.error)
                }
            }
        }
    }
    @Composable
    fun tray(vertical: Boolean = false) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.recipe_tray_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.recipe_tray_hint), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (vertical) LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(data?.recipes.orEmpty(), key = { it.id }) { recipe ->
                    RecipeCard(recipe, { onAction(GameAction.ViewRecipe(recipe)) }, { onAction(GameAction.Pick(recipe)) }, dragModifier(recipe, null), compact = true)
                }
            } else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(data?.recipes.orEmpty(), key = { it.id }) { recipe ->
                    Column(Modifier.width(188.dp).then(dragModifier(recipe, null)).background(Surface.copy(alpha = 0.94f), RoundedCornerShape(14.dp)).padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RecipeImage(recipe, 40.dp)
                            Text(recipe.title.localized(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                        ActionText(R.string.add_to_day, { onAction(GameAction.Pick(recipe)) }, enabled = !plan.saving && !plan.error)
                    }
                }
            }
        }
    }
    @Composable
    fun weekControls() {
        Column {
            Text(weekLabel(plan.summary.week), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ActionText(R.string.previous, { onAction(GameAction.ShiftWeek(-1)) }, enabled = !plan.saving && !plan.error) }
                item { ActionText(R.string.today, { onAction(GameAction.Today) }, enabled = !plan.saving && !plan.error) }
                item { ActionText(R.string.next, { onAction(GameAction.ShiftWeek(1)) }, enabled = !plan.saving && !plan.error) }
                item { ActionText(R.string.copy_previous_week, { onAction(GameAction.CopyAsk) }, enabled = !plan.saving && !plan.error) }
            }
        }
    }
    @Composable
    fun footer() {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(if (plan.summary.dinners.isEmpty()) R.string.empty_plan_hint else R.string.plan_hint, plan.summary.dinners.size), style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            PrimaryButton(R.string.build_shopping_list, { onAction(GameAction.Build) }, enabled = plan.summary.dinners.isNotEmpty() && !plan.saving && !plan.error, busy = plan.saving)
        }
    }
    AppBackdrop(2) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val narrowHeight = maxHeight < 540.dp || density.fontScale > 1.4f
            val wide = maxWidth >= 600.dp
            Column(Modifier.widthIn(max = 840.dp).fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (narrowHeight) LazyColumn(state = boardState, modifier = Modifier.fillMaxSize().onGloballyPositioned { boardBounds = it.boundsInRoot() }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { ScreenHeader(R.string.week_plan, { onAction(GameAction.Back) }, R.string.recipes, { onAction(GameAction.Recipes) }) }
                    item { weekControls() }
                    item { InlineStatus(plan, { onAction(GameAction.Retry) }, { onAction(GameAction.Discard) }) }
                    item { tray() }
                    items(7, key = { plan.summary.week.plusDays(it.toLong()).toString() }) { dayCard(plan.summary.week.plusDays(it.toLong())) }
                    item { footer() }
                } else {
                    ScreenHeader(R.string.week_plan, { onAction(GameAction.Back) }, R.string.recipes, { onAction(GameAction.Recipes) })
                    weekControls()
                    if (plan.saving || plan.error || plan.loading || plan.checksChanged) InlineStatus(plan, { onAction(GameAction.Retry) }, { onAction(GameAction.Discard) })
                    if (wide) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LazyColumn(state = boardState, modifier = Modifier.weight(0.6f).onGloballyPositioned { boardBounds = it.boundsInRoot() }, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 6.dp)) {
                            items(7, key = { plan.summary.week.plusDays(it.toLong()).toString() }) { dayCard(plan.summary.week.plusDays(it.toLong())) }
                        }
                        Box(Modifier.weight(0.4f)) { tray(true) }
                    } else {
                        LazyColumn(state = boardState, modifier = Modifier.weight(1f).onGloballyPositioned { boardBounds = it.boundsInRoot() }, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 6.dp)) {
                            items(7, key = { plan.summary.week.plusDays(it.toLong()).toString() }) { dayCard(plan.summary.week.plusDays(it.toLong())) }
                        }
                        tray()
                    }
                    footer()
                }
            }
        }
        val active = drag
        if (active != null) Box(Modifier.offset { IntOffset((active.point.x - rootOrigin.x - 74.dp.toPx()).roundToInt(), (active.point.y - rootOrigin.y - 45.dp.toPx()).roundToInt()) }.width(148.dp).graphicsLayer { scaleX = 1.03f; scaleY = 1.03f; shadowElevation = 10.dp.toPx() }) {
            ProduceCard { RecipeImage(active.recipe, 44.dp); Text(active.recipe.title.localized(), style = MaterialTheme.typography.bodyMedium) }
        }
    }
    if (state.pickerRecipe != null && data != null) DayPickerSheet(data, { onAction(GameAction.Choose(it)) }, { onAction(GameAction.Dismiss) })
    val pending = state.pending
    if (pending != null) ConfirmationDialog(R.string.replace_title,
        stringResource(R.string.replace_message, data?.dinners?.get(pending.date)?.snapshot?.recipe?.title?.localized() ?: stringResource(R.string.unassigned), dayLabel(pending.date), pending.recipe.title.localized()),
        R.string.replace, { onAction(GameAction.Confirm) }, { onAction(GameAction.Dismiss) })
    val focused = state.focusedDay?.let { data?.dinners?.get(it) }
    if (focused != null) AlertDialog(onDismissRequest = { onAction(GameAction.Dismiss) }, title = { Text(focused.snapshot.recipe.title.localized()) },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { ServingStepper(focused.servings, { onAction(GameAction.Servings(it)) }, !plan.saving)
            ActionText(R.string.view_recipe, { onAction(GameAction.ViewDinner(focused.date)) })
            ActionText(R.string.move_dinner, { onAction(GameAction.Move) }, enabled = !plan.saving)
            ActionText(R.string.replace, { onAction(GameAction.Replace) }, enabled = !plan.saving)
            ActionText(R.string.remove, { onAction(GameAction.RemoveAsk) }, enabled = !plan.saving)
        } }, confirmButton = { ActionText(R.string.okay, { onAction(GameAction.Dismiss) }) })
    if (state.removeDate != null) ConfirmationDialog(R.string.remove_dinner_title, stringResource(R.string.remove_dinner_message), R.string.remove, { onAction(GameAction.Remove) }, { onAction(GameAction.Dismiss) })
    if (state.copyConfirmation) ConfirmationDialog(R.string.copy_title, stringResource(R.string.copy_message), R.string.copy_confirm, { onAction(GameAction.CopyConfirm) }, { onAction(GameAction.Dismiss) })
    if (state.noPrevious) ConfirmationDialog(R.string.no_previous_title, stringResource(R.string.no_previous_message), R.string.okay, { onAction(GameAction.Dismiss) }, { onAction(GameAction.Dismiss) })
    if (state.leaveConfirmation) ConfirmationDialog(R.string.leave_title, stringResource(R.string.leave_message), R.string.discard, { onAction(GameAction.Leave) }, { onAction(GameAction.Dismiss) }, R.string.keep_editing)
}
