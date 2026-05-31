package com.example.data.database

import androidx.room.*
import com.example.data.model.Note
import com.example.data.model.TodoTask
import com.example.data.model.Tracker
import com.example.data.model.TrackerLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- NOTES CRUD ---
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // --- TODO TASKS CRUD ---
    @Query("SELECT * FROM todo_tasks ORDER BY priorityOrder ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<TodoTask>>

    @Query("SELECT MAX(priorityOrder) FROM todo_tasks")
    suspend fun getMaxPriorityOrder(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodoTask)

    @Update
    suspend fun updateTask(task: TodoTask)

    @Delete
    suspend fun deleteTask(task: TodoTask)

    // --- TRACKERS CRUD ---
    @Query("SELECT * FROM trackers ORDER BY createdAt DESC")
    fun getAllTrackers(): Flow<List<Tracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker): Long

    @Delete
    suspend fun deleteTracker(tracker: Tracker)

    // --- TRACKER LOGS ---
    @Query("SELECT * FROM tracker_logs ORDER BY timestamp DESC")
    fun getAllTrackerLogs(): Flow<List<TrackerLog>>

    @Query("SELECT * FROM tracker_logs WHERE trackerId = :trackerId ORDER BY timestamp DESC")
    fun getTrackerLogsForId(trackerId: Int): Flow<List<TrackerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackerLog(log: TrackerLog)

    @Query("DELETE FROM tracker_logs WHERE trackerId = :trackerId AND logDate = :logDate")
    suspend fun deleteTrackerLog(trackerId: Int, logDate: String)
}
