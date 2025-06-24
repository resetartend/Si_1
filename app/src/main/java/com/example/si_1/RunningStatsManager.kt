package com.example.si_1

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object RunningStatsManager {
    private const val PREFS_NAME = "RunningStats"
    private val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun saveRun(context: Context, courseDistanceKm: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val today = sdfDate.format(Date())
        val month = sdfMonth.format(Date())

        val lastDate = prefs.getString("lastRunDate", "")
        val lastMonth = prefs.getString("lastRunMonth", "")

        val todayDistance = if (today == lastDate) prefs.getFloat("todayDistance", 0f) else 0f
        val monthDistance = if (month == lastMonth) prefs.getFloat("monthDistance", 0f) else 0f

        val totalDistance = prefs.getFloat("totalDistance", 0f)
        val runCount = prefs.getInt("runCount", 0)
        val bestRecord = prefs.getFloat("bestRecord", 0f)

        val newToday = todayDistance + courseDistanceKm.toFloat()
        val newMonth = monthDistance + courseDistanceKm.toFloat()
        val newTotal = totalDistance + courseDistanceKm.toFloat()
        val newBest = maxOf(bestRecord, courseDistanceKm.toFloat())

        editor.putFloat("todayDistance", newToday)
        editor.putFloat("monthDistance", newMonth)
        editor.putFloat("totalDistance", newTotal)
        editor.putFloat("bestRecord", maxOf(bestRecord, newMonth))
        editor.putInt("runCount", runCount + 1)
        editor.putString("lastRunDate", today)
        editor.putString("lastRunMonth", month)

        editor.apply()
    }

    fun getStats(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = sdfDate.format(Date())
        val month = sdfMonth.format(Date())

        val todayDistance = if (prefs.getString("lastRunDate", "") == today)
            prefs.getFloat("todayDistance", 0f) else 0f

        val monthDistance = if (prefs.getString("lastRunMonth", "") == month)
            prefs.getFloat("monthDistance", 0f) else 0f

        return mapOf(
            "runCount" to prefs.getInt("runCount", 0),
            "totalDistance" to prefs.getFloat("totalDistance", 0f),
            "todayDistance" to todayDistance,
            "monthDistance" to monthDistance,
            "bestRecord" to prefs.getFloat("bestRecord", 0f)
        )
    }
}
