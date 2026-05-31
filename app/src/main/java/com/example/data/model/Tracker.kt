package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackers")
data class Tracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "DAILY_HABIT", "WEEKLY_GOAL", "MONTHLY_GOAL", "YEARLY_GOAL"
    val targetValue: Int = 1, // number of times expected (e.g., 7 times a week, or 1 for standard goals)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tracker_logs")
data class TrackerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackerId: Int,
    val logDate: String, // String representation format "yyyy-MM-dd" for calendar mapping
    val timestamp: Long = System.currentTimeMillis()
)
