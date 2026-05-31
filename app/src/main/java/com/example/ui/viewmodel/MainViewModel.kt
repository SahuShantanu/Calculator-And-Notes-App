package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Note
import com.example.data.model.TodoTask
import com.example.data.model.Tracker
import com.example.data.model.TrackerLog
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao)
    }

    // --- SLEEK UI PREFERENCE STATE ---
    var isDarkMode by mutableStateOf(true) // Sleek dark mode by default!

    // --- REACTIVE DB STATE FLOWS ---
    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TodoTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackers: StateFlow<List<Tracker>> = repository.allTrackers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackerLogs: StateFlow<List<TrackerLog>> = repository.allTrackerLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- NOTES BUSINESS LOGIC ---
    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(
                Note(
                    title = title,
                    content = content,
                    category = category,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- TODO TASKS PRIORITIZATION & EDITING ---
    fun addTask(title: String, priorityLevel: String) {
        viewModelScope.launch {
            val maxOrder = repository.getMaxPriorityOrder()
            repository.insertTask(
                TodoTask(
                    title = title,
                    isCompleted = false,
                    priorityOrder = maxOrder + 1,
                    priorityLevel = priorityLevel,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TodoTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun moveTaskUp(task: TodoTask, currentList: List<TodoTask>) {
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index > 0) {
            viewModelScope.launch {
                repository.swapTaskPositions(task, currentList[index - 1])
            }
        }
    }

    fun moveTaskDown(task: TodoTask, currentList: List<TodoTask>) {
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index != -1 && index < currentList.size - 1) {
            viewModelScope.launch {
                repository.swapTaskPositions(task, currentList[index + 1])
            }
        }
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- TRACKERS AND PROGRESS LOGS ---
    fun addTracker(title: String, type: String, targetValue: Int) {
        viewModelScope.launch {
            repository.insertTracker(
                Tracker(
                    title = title,
                    type = type,
                    targetValue = targetValue
                )
            )
        }
    }

    fun deleteTracker(tracker: Tracker) {
        viewModelScope.launch {
            repository.deleteTracker(tracker)
        }
    }

    fun toggleHabitCheckIn(trackerId: Int, dateString: String, logsOnDate: List<TrackerLog>) {
        viewModelScope.launch {
            val logged = logsOnDate.find { it.trackerId == trackerId && it.logDate == dateString }
            if (logged != null) {
                repository.deleteTrackerLog(trackerId, dateString)
            } else {
                repository.insertTrackerLog(
                    TrackerLog(
                        trackerId = trackerId,
                        logDate = dateString,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun incrementTrackerCheckIn(trackerId: Int, dateString: String) {
        viewModelScope.launch {
            repository.insertTrackerLog(
                TrackerLog(
                    trackerId = trackerId,
                    logDate = dateString,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun decrementTrackerCheckIn(trackerId: Int, dateString: String, logsOnDate: List<TrackerLog>) {
        viewModelScope.launch {
            val matchingLog = logsOnDate.find { it.trackerId == trackerId && it.logDate == dateString }
            if (matchingLog != null) {
                // Delete just one item
                // In SQLite we can delete by ID
                repository.deleteTrackerLog(trackerId, dateString) // simple delete by date
            }
        }
    }

    // --- CALCULATOR ENGINE ---
    var calcInput by mutableStateOf("")
    var calcResult by mutableStateOf("")
    val calcHistory = mutableStateListOf<String>()

    fun onCalcButtonPressed(button: String) {
        when (button) {
            "C" -> {
                calcInput = ""
                calcResult = ""
            }
            "⌫" -> {
                if (calcInput.isNotEmpty()) {
                    calcInput = calcInput.substring(0, calcInput.length - 1)
                }
            }
            "=" -> {
                if (calcInput.isNotEmpty()) {
                    try {
                        val parsed = evaluateExpression(calcInput)
                        calcResult = formatResult(parsed)
                        val historyEntry = "$calcInput = $calcResult"
                        if (calcHistory.size >= 25) {
                            calcHistory.removeAt(0)
                        }
                        calcHistory.add(historyEntry)
                    } catch (e: Exception) {
                        calcResult = "Error"
                    }
                }
            }
            else -> {
                // Prevent duplicate consecutive operation symbols if needed
                if (isOperator(button) && calcInput.isNotEmpty() && isOperator(calcInput.last().toString())) {
                    calcInput = calcInput.dropLast(1) + button
                } else {
                    calcInput += button
                }
            }
        }
    }

    private fun isOperator(s: String): Boolean {
        return s == "+" || s == "-" || s == "×" || s == "÷" || s == "%" || s == "^" || s == "√"
    }

    private fun formatResult(value: Double): String {
        return if (value.isNaN()) {
            "Error"
        } else if (value.isInfinite()) {
            "Infinity"
        } else if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.5f", value).trimEnd('0').trimEnd('.')
        }
    }

    // A robust recursive descent expression parser in Kotlin
    private fun evaluateExpression(expr: String): Double {
        // Sanitize string to standard math operators
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < sanitized.length) sanitized[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < sanitized.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            // Expression = Term + Term | Term - Term
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            // Term = Power * Power | Power / Power
            fun parseTerm(): Double {
                var x = parsePower()
                while (true) {
                    if (eat('*'.code)) x *= parsePower() // multiplication
                    else if (eat('/'.code)) {
                        val denominator = parsePower()
                        if (denominator == 0.0) throw ArithmeticException("Division by zero")
                        x /= denominator // division
                    } else if (eat('%'.code)) {
                        x %= parsePower()
                    } else return x
                }
            }

            // Power = Factor ^ Power | Factor
            fun parsePower(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('^'.code)) {
                        x = Math.pow(x, parsePower())
                    } else return x
                }
            }

            // Factor = +Factor | -Factor | √Factor | (Expression) | Number
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus
                if (eat('√'.code)) return Math.sqrt(parseFactor()) // square root support

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    val numStr = sanitized.substring(startPos, this.pos)
                    x = numStr.toDouble()
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }
}
