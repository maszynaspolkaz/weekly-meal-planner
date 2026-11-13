package com.weeklymeal.planner.presentation.recipes

import com.weeklymeal.planner.core.navigation.AppEvent
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.repository.PlannerRepository
import com.weeklymeal.planner.domain.usecase.AggregateIngredients
import com.weeklymeal.planner.domain.usecase.ValidateRecipe
import com.weeklymeal.planner.presentation.app.PlannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

class RecipesViewModel(repository: PlannerRepository, aggregate: AggregateIngredients, private val resolve: (Label) -> String) : PlannerViewModel(repository, aggregate) {
    private val _state = MutableStateFlow(RecipesUiState())
    val state = _state.asStateFlow()
    private val validate = ValidateRecipe()
    fun editorAction(action: EditorAction) {
        when (action) {
            is EditorAction.Title -> title(action.value)
            is EditorAction.SetCategory -> recipeCategory(action.value)
            is EditorAction.Duration -> duration(action.value)
            is EditorAction.Servings -> servings(action.value)
            is EditorAction.Image -> image(action.value)
            is EditorAction.AddIngredient -> addIngredient(action.value)
            is EditorAction.RemoveIngredient -> removeIngredient(action.id)
            is EditorAction.Amount -> amount(action.id, action.direction)
            is EditorAction.Unit -> unit(action.id)
            is EditorAction.Step -> step(action.index, action.value)
            is EditorAction.MoveStep -> moveStep(action.index, action.direction)
            is EditorAction.RemoveStep -> removeStep(action.index)
            is EditorAction.IngredientQuery -> ingredientQuery(action.value)
            is EditorAction.NewIngredient -> ingredientDraft(action.value)
            EditorAction.AddStep -> addStep()
            EditorAction.OpenIngredients -> ingredientPicker(true)
            EditorAction.CloseIngredients -> ingredientPicker(false)
            EditorAction.CreateIngredient -> createIngredient()
            EditorAction.SaveIngredient -> saveIngredient()
            EditorAction.Save -> save()
            EditorAction.Cancel -> cancelEdit()
            EditorAction.Discard -> discardEditor()
            EditorAction.KeepEditing -> keepEditing()
        }
    }
    init { observe { plan -> _state.update { it.copy(plan = plan.copy(saving = it.plan.saving)) }; filter() } }
    private fun progress(saving: Boolean, error: Boolean) { _state.update { it.copy(plan = it.plan.copy(saving = saving, error = error, saved = !saving && !error)) } }
    fun enter(target: LocalDate?) { _state.update { it.copy(targetDate = target) } }
    fun query(value: String) { _state.update { it.copy(query = value) }; filter() }
    fun category(value: Category?) { _state.update { it.copy(category = value) }; filter() }
    fun sort(value: RecipeSort) { _state.update { it.copy(sort = value) }; filter() }
    fun clearFilters() { _state.update { it.copy(query = "", category = null) }; filter() }
    private fun filter() {
        val state = _state.value
        val data = state.plan.data ?: return
        val query = state.query.trim()
        val matches = data.recipes.filter { recipe ->
            (state.category == null || recipe.category == state.category) &&
                (resolve(recipe.title).contains(query, true) || recipe.ingredients.any { line -> data.ingredientCatalog.find { it.id == line.ingredientId }?.let { resolve(it.name).contains(query, true) } == true })
        }
        val sorted = when (state.sort) {
            RecipeSort.NAME -> matches.sortedBy { resolve(it.title).lowercase(Locale.ROOT) }
            RecipeSort.QUICKEST -> matches.sortedBy { it.minutes }
            RecipeSort.RECENT -> matches.sortedByDescending { it.updatedAt }
        }
        _state.update { it.copy(visible = sorted) }
    }
    fun edit(recipe: Recipe? = null) {
        val draft = recipe ?: Recipe(UUID.randomUUID().toString(), Label(""), Category.VEGETARIAN, 25, 2, "sprite_1", emptyList(), listOf(Label("")))
        _state.update { it.copy(editor = draft, original = draft, extraIngredients = emptyList(), invalid = false, discardDialog = false, largeUnits = emptySet()) }
    }
    fun change(transform: (Recipe) -> Recipe) { _state.update { state -> state.editor?.let { state.copy(editor = transform(it), invalid = false) } ?: state } }
    fun title(value: String) = change { it.copy(title = Label(value.take(60))) }
    fun recipeCategory(value: Category) = change { it.copy(category = value) }
    fun duration(value: Int) = change { it.copy(minutes = value.coerceIn(5, 180)) }
    fun servings(value: Int) = change { it.copy(baseServings = value.coerceIn(1, 12)) }
    fun image(value: String) = change { it.copy(imageKey = value) }
    fun ingredientPicker(show: Boolean) { _state.update { it.copy(picker = show, ingredientDraft = null, ingredientError = false) } }
    fun ingredientQuery(value: String) { _state.update { it.copy(ingredientQuery = value) } }
    fun addIngredient(ingredient: IngredientDefinition) {
        change { recipe ->
            if (recipe.ingredients.size >= 40 || recipe.ingredients.any { it.ingredientId == ingredient.id }) recipe
            else recipe.copy(ingredients = recipe.ingredients + RecipeIngredient(ingredient.id, if (ingredient.measurement == MeasurementType.COUNT) 1L else 100L))
        }
        ingredientPicker(false)
    }
    fun removeIngredient(id: String) = change { it.copy(ingredients = it.ingredients.filterNot { line -> line.ingredientId == id }) }
    fun unit(id: String) { _state.update { it.copy(largeUnits = if (id in it.largeUnits) it.largeUnits - id else it.largeUnits + id) } }
    fun amount(id: String, direction: Int) {
        val catalog = _state.value.plan.data?.ingredientCatalog.orEmpty() + _state.value.extraIngredients
        val definition = catalog.find { it.id == id } ?: return
        val count = definition.measurement == MeasurementType.COUNT
        val increment = if (count) 1L else 25L
        val limit = if (count) 1000L else 100000L
        change { it.copy(ingredients = it.ingredients.map { line -> if (line.ingredientId == id) line.copy(amount = (line.amount + increment * direction).coerceIn(1L, limit)) else line }) }
    }
    fun step(index: Int, value: String) = change { it.copy(steps = it.steps.mapIndexed { i, label -> if (i == index) Label(value.take(500)) else label }) }
    fun addStep() = change { if (it.steps.size < 20) it.copy(steps = it.steps + Label("")) else it }
    fun removeStep(index: Int) = change { it.copy(steps = it.steps.filterIndexed { i, _ -> i != index }) }
    fun moveStep(index: Int, offset: Int) = change {
        val target = index + offset
        if (target !in it.steps.indices) it else {
            val steps = it.steps.toMutableList()
            val value = steps.removeAt(index)
            steps.add(target, value)
            it.copy(steps = steps.toList())
        }
    }
    fun createIngredient() { _state.update { it.copy(ingredientDraft = IngredientDraft(), ingredientError = false) } }
    fun ingredientDraft(value: IngredientDraft) { _state.update { it.copy(ingredientDraft = value, ingredientError = false) } }
    fun saveIngredient() {
        val state = _state.value
        val draft = state.ingredientDraft ?: return
        val name = draft.name.trim().replace(Regex("\\s+"), " ")
        if (name.length !in 1..40) { _state.update { it.copy(ingredientError = true) }; return }
        val existing = (state.plan.data?.ingredientCatalog.orEmpty() + state.extraIngredients).find { resolve(it.name).trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT) == name.lowercase(Locale.ROOT) }
        if (existing != null) {
            if (existing.measurement != draft.measurement) { _state.update { it.copy(ingredientError = true) }; return }
            addIngredient(existing)
        } else {
            val ingredient = IngredientDefinition(UUID.randomUUID().toString(), Label(name), draft.measurement, draft.section)
            _state.update { it.copy(extraIngredients = it.extraIngredients + ingredient) }
            addIngredient(ingredient)
        }
    }
    fun save() {
        val state = _state.value
        val recipe = state.editor ?: return
        val catalog = state.plan.data?.ingredientCatalog.orEmpty() + state.extraIngredients
        if (!validate(recipe, catalog)) { _state.update { it.copy(invalid = true) }; return }
        val normalized = validate.normalize(recipe).copy(updatedAt = System.currentTimeMillis())
        write(::progress, { _state.update { it.copy(editor = null, original = null, extraIngredients = emptyList(), invalid = false) } }) {
            it.copy(recipes = it.recipes.filterNot { old -> old.id == normalized.id } + normalized, ingredientCatalog = it.ingredientCatalog + state.extraIngredients.filterNot { added -> it.ingredientCatalog.any { old -> old.id == added.id } })
        }
    }
    fun cancelEdit() {
        if (_state.value.plan.saving) return
        if (_state.value.dirty) _state.update { it.copy(discardDialog = true) } else discardEditor()
    }
    fun keepEditing() { _state.update { it.copy(discardDialog = false) } }
    fun discardEditor() {
        discardFailure()
        _state.update { it.copy(editor = null, original = null, extraIngredients = emptyList(), picker = false, ingredientDraft = null, discardDialog = false, invalid = false, plan = current()) }
    }
    fun deleteAsk(recipe: Recipe?) { _state.update { it.copy(deleteCandidate = recipe) } }
    fun delete() {
        val id = _state.value.deleteCandidate?.id ?: return
        _state.update { it.copy(deleteCandidate = null) }
        write(::progress) { it.copy(recipes = it.recipes.filterNot { recipe -> recipe.id == id }) }
    }
    fun schedule(recipe: Recipe) {
        val date = _state.value.targetDate
        _state.update { it.copy(dayRecipe = recipe) }
        if (date != null) chooseDay(date)
    }
    fun chooseDay(date: LocalDate, confirmed: Boolean = false) {
        val state = _state.value
        val recipe = state.dayRecipe ?: return
        if (!confirmed && state.plan.data?.dinners?.containsKey(date) == true) { _state.update { it.copy(pendingDate = date) }; return }
        _state.update { it.copy(pendingDate = null, dayRecipe = null) }
        write(::progress, { if (state.targetDate != null) emit(AppEvent.Back) }) {
            it.copy(dinners = it.dinners + (date to PlannedDinner(date, RecipeSnapshot(recipe.id, recipe), recipe.baseServings)))
        }
    }
    fun dismissPicker() { _state.update { it.copy(dayRecipe = null, pendingDate = null) } }
    override fun back() {
        val state = _state.value
        when {
            state.plan.saving -> Unit
            state.picker -> ingredientPicker(false)
            state.editor != null -> cancelEdit()
            state.dayRecipe != null -> dismissPicker()
            state.deleteCandidate != null -> deleteAsk(null)
            else -> super.back()
        }
    }
}
