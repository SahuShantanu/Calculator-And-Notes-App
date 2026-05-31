package com.example.data.repository

import com.example.data.database.AppDao
import com.example.data.model.Note
import com.example.data.model.TodoTask
import com.example.data.model.Tracker
import com.example.data.model.TrackerLog
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- NOTES REST ---
    val allNotes: Flow<List<Note>> = appDao.getAllNotes()

    suspend fun insertNote(note: Note) = appDao.insertNote(note)

    suspend fun updateNote(note: Note) = appDao.updateNote(note)

    suspend fun deleteNote(note: Note) = appDao.deleteNote(note)

    // --- TODO TASKS ---
    val allTasks: Flow<List<TodoTask>> = appDao.getAllTasks()

    suspend fun insertTask(task: TodoTask) = appDao.insertTask(task)

    suspend fun updateTask(task: TodoTask) = appDao.updateTask(task)

    suspend fun deleteTask(task: TodoTask) = appDao.deleteTask(task)

    suspend fun getMaxPriorityOrder(): Int = appDao.getMaxPriorityOrder() ?: 0

    suspend fun swapTaskPositions(task1: TodoTask, task2: TodoTask) {
        val tempOrder = task1.priorityOrder
        val updatedTask1 = task1.copy(priorityOrder = task2.priorityOrder)
        val updatedTask2 = task2.copy(priorityOrder = tempOrder)
        appDao.updateTask(updatedTask1)
        appDao.updateTask(updatedTask2)
    }

    // --- TRACKERS & LOGS ---
    val allTrackers: Flow<List<Tracker>> = appDao.getAllTrackers()
    val allTrackerLogs: Flow<List<TrackerLog>> = appDao.getAllTrackerLogs()

    suspend fun insertTracker(tracker: Tracker): Long = appDao.insertTracker(tracker)

    suspend fun deleteTracker(tracker: Tracker) = appDao.deleteTracker(tracker)

    suspend fun insertTrackerLog(log: TrackerLog) = appDao.insertTrackerLog(log)

    suspend fun deleteTrackerLog(trackerId: Int, logDate: String) = 
        appDao.deleteTrackerLog(trackerId, logDate)
}
