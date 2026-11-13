package com.weeklymeal.planner.data.local

import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.domain.usecase.ValidateRecipe
import com.weeklymeal.planner.data.sample.SampleData
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class PlannerJsonCodec {
    private val knownLabels: Set<String> by lazy {
        val sample = SampleData.create()
        (sample.ingredientCatalog.map { it.name } + sample.recipes.flatMap { listOf(it.title) + it.steps }).map { it.value }.toSet()
    }
    private fun JSONObject.strictLong(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long)
        return (value as Number).toLong()
    }
    private fun JSONObject.strictInt(key: String): Int {
        val value = strictLong(key)
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        return value.toInt()
    }
    private fun label(value: Label) = JSONObject().put("value", value.value).put("resource", value.resource)
    private fun readLabel(value: JSONObject): Label {
        val label = Label(value.getString("value"), value.getBoolean("resource"))
        require(!label.resource || label.value in knownLabels)
        return label
    }
    private fun <T> array(values: Iterable<T>, encode: (T) -> Any): JSONArray = JSONArray().also { json -> values.forEach { json.put(encode(it)) } }
    private fun <T> readArray(values: JSONArray, decode: (JSONObject) -> T): List<T> = (0 until values.length()).map { decode(values.getJSONObject(it)) }
    private fun recipe(value: Recipe): JSONObject = JSONObject()
        .put("id", value.id).put("title", label(value.title)).put("category", value.category.name)
        .put("minutes", value.minutes).put("baseServings", value.baseServings).put("imageKey", value.imageKey).put("updatedAt", value.updatedAt)
        .put("ingredients", array(value.ingredients) { JSONObject().put("id", it.ingredientId).put("amount", it.amount) })
        .put("steps", array(value.steps) { label(it) })

    private fun readRecipe(value: JSONObject) = Recipe(
        value.getString("id"), readLabel(value.getJSONObject("title")), Category.valueOf(value.getString("category")),
        value.strictInt("minutes"), value.strictInt("baseServings"), value.getString("imageKey"),
        readArray(value.getJSONArray("ingredients")) { RecipeIngredient(it.getString("id"), it.strictLong("amount")) },
        readArray(value.getJSONArray("steps")) { readLabel(it) }, value.strictLong("updatedAt")
    )

    fun encode(data: PlannerData): String = JSONObject()
        .put("schemaVersion", data.schemaVersion).put("seedVersion", data.seedVersion)
        .put("weekStart", data.weekStart.name).put("selectedWeek", data.selectedWeek.toString()).put("revision", data.revision).put("followCurrentWeek", data.followCurrentWeek)
        .put("recipes", array(data.recipes) { recipe(it) })
        .put("catalog", array(data.ingredientCatalog) { JSONObject().put("id", it.id).put("name", label(it.name)).put("measurement", it.measurement.name).put("section", it.section.name) })
        .put("dinners", array(data.dinners.values) { JSONObject().put("date", it.date.toString()).put("source", it.snapshot.sourceRecipeId).put("recipe", recipe(it.snapshot.recipe)).put("servings", it.servings) })
        .put("checks", array(data.checks.entries) { entry ->
            JSONObject().put("key", entry.key).put("items", array(entry.value.entries) { item ->
                JSONObject().put("id", item.key).put("amount", item.value.amount).put("revision", item.value.revision).put("atHome", item.value.atHome).put("purchased", item.value.purchased)
            })
        }).toString()

    fun decode(payload: String): PlannerData {
        val root = JSONObject(payload)
        require(root.strictInt("schemaVersion") == 1 && root.strictInt("seedVersion") == 1)
        val catalog = readArray(root.getJSONArray("catalog")) {
            IngredientDefinition(it.getString("id"), readLabel(it.getJSONObject("name")), MeasurementType.valueOf(it.getString("measurement")), StoreSection.valueOf(it.getString("section")))
        }
        require(catalog.isNotEmpty() && catalog.map { it.id }.distinct().size == catalog.size)
        require(catalog.all { it.id.isNotBlank() && it.name.value.isNotBlank() && (it.name.resource || it.name.value.length <= 40) })
        val recipes = readArray(root.getJSONArray("recipes")) { readRecipe(it) }
        val dinners = readArray(root.getJSONArray("dinners")) {
            PlannedDinner(LocalDate.parse(it.getString("date")), RecipeSnapshot(it.getString("source"), readRecipe(it.getJSONObject("recipe"))), it.strictInt("servings"))
        }
        require(recipes.map { it.id }.distinct().size == recipes.size && dinners.map { it.date }.distinct().size == dinners.size)
        require((recipes + dinners.map { it.snapshot.recipe }).all { ValidateRecipe()(it, catalog) && it.imageKey in listOf("sprite_1", "sprite_2", "sprite_3", "sprite_4", "sprite_5") })
        require(dinners.all { it.servings in 1..12 && it.snapshot.sourceRecipeId == it.snapshot.recipe.id })
        val groups = readArray(root.getJSONArray("checks")) { group ->
            val key = group.getString("key")
            val start = WeekStart.valueOf(key.substringBefore(':'))
            val date = LocalDate.parse(key.substringAfter(':'))
            require(weekContaining(date, start) == date)
            val items = readArray(group.getJSONArray("items")) { item ->
                val check = RequirementCheck(item.strictLong("amount"), item.strictLong("revision"), item.getBoolean("atHome"), item.getBoolean("purchased"))
                require(check.amount > 0 && check.revision in 0..root.strictLong("revision") && !(check.atHome && check.purchased))
                val id = item.getString("id")
                require(catalog.any { it.id == id })
                id to check
            }
            require(items.map { it.first }.distinct().size == items.size)
            key to items.toMap()
        }
        require(groups.map { it.first }.distinct().size == groups.size)
        val checks = groups.toMap()
        val weekStart = WeekStart.valueOf(root.getString("weekStart"))
        val week = LocalDate.parse(root.getString("selectedWeek"))
        require(weekContaining(week, weekStart) == week && root.strictLong("revision") >= 0)
        require(checks.keys.all { it.startsWith(weekStart.name + ":") })
        return PlannerData(recipes = recipes, ingredientCatalog = catalog, dinners = dinners.associateBy { it.date }, checks = checks, weekStart = weekStart, selectedWeek = week, revision = root.strictLong("revision"), followCurrentWeek = root.optBoolean("followCurrentWeek", false))
    }
}
