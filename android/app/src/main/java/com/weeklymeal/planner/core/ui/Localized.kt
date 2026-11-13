package com.weeklymeal.planner.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.text.labelResource
import androidx.compose.ui.platform.LocalContext
import com.weeklymeal.planner.domain.model.*
import com.weeklymeal.planner.presentation.recipes.RecipeSort
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun labelSorter(): (Label) -> String {
    val resources = LocalContext.current.resources
    return { label -> (if (label.resource) resources.getString(labelResource(label.value)) else label.value).lowercase(Locale.ROOT) }
}

@Composable
fun Label.localized(): String = if (resource) stringResource(labelResource(value)) else value

fun categoryResource(category: Category): Int = when (category) {
    Category.VEGETARIAN -> R.string.category_vegetarian
    Category.CHICKEN -> R.string.category_chicken
    Category.FISH -> R.string.category_fish
    Category.OTHER -> R.string.category_other
}

fun sectionResource(section: StoreSection): Int = when (section) {
    StoreSection.PRODUCE -> R.string.section_produce
    StoreSection.MEAT_FISH -> R.string.section_meat_fish
    StoreSection.DAIRY_EGGS -> R.string.section_dairy_eggs
    StoreSection.CANNED -> R.string.section_canned
    StoreSection.GRAINS -> R.string.section_grains
    StoreSection.OILS_SPICES -> R.string.section_oils_spices
}

fun sortResource(sort: RecipeSort): Int = when (sort) {
    RecipeSort.NAME -> R.string.sort_name
    RecipeSort.QUICKEST -> R.string.sort_quickest
    RecipeSort.RECENT -> R.string.sort_recent
}

@Composable
fun weekLabel(week: LocalDate): String = stringResource(R.string.week_range,
    week.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())),
    week.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())))

@Composable
fun dayLabel(date: LocalDate): String = stringResource(R.string.day_date, date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())))

@Composable
fun quantityLabel(amount: Long, measurement: MeasurementType, preferLarge: Boolean = false): String {
    if (measurement == MeasurementType.COUNT) return pluralStringResource(R.plurals.quantity_items, amount.toInt(), amount)
    if (amount < 1000L && !preferLarge) return stringResource(if (measurement == MeasurementType.MASS) R.string.quantity_g else R.string.quantity_ml, amount)
    val precision = if (preferLarge) 3 else 2
    val value = BigDecimal.valueOf(amount).divide(BigDecimal(1000), precision, RoundingMode.CEILING)
    val number = NumberFormat.getNumberInstance().apply { maximumFractionDigits = precision }.format(value)
    return stringResource(if (measurement == MeasurementType.MASS) R.string.quantity_kg else R.string.quantity_l, number)
}

@Composable
fun contributionLabel(numerator: Long, measurement: MeasurementType): String {
    val amount = BigDecimal.valueOf(numerator).divide(BigDecimal(27720), 2, RoundingMode.CEILING)
    val number = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 2 }.format(amount)
    return stringResource(R.string.amount_unit, number, stringResource(when (measurement) {
        MeasurementType.MASS -> R.string.unit_g
        MeasurementType.VOLUME -> R.string.unit_ml
        MeasurementType.COUNT -> R.string.unit_count
    }))
}
