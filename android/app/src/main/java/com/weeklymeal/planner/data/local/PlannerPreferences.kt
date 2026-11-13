package com.weeklymeal.planner.data.local

import android.content.Context
import java.io.IOException

class PlannerPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("weekly_meal_planner", Context.MODE_PRIVATE)
    fun read(): String? = preferences.getString("planner_payload", null)
    fun write(payload: String) {
        if (!preferences.edit().putString("planner_payload", payload).commit()) throw IOException()
    }
}
