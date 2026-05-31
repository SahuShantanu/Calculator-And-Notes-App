package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_tasks")
data class TodoTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priorityOrder: Int, // lower standard values are higher priority, or vice versa (we'll sort by priorityOrder asc)
    val priorityLevel: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val timestamp: Long = System.currentTimeMillis()
)
