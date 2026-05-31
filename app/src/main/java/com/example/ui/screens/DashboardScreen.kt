package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Tracker
import com.example.data.model.TrackerLog
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val trackers by viewModel.trackers.collectAsState()
    val logs by viewModel.trackerLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTrackerForChart by remember { mutableStateOf<Tracker?>(null) }

    // If there is any tracker and none is selected for the chart, select the first one by default
    LaunchedEffect(trackers) {
        if (selectedTrackerForChart == null && trackers.isNotEmpty()) {
            selectedTrackerForChart = trackers.first()
        }
    }

    val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // --- HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Goal Trackers",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Visualize progress on habits and goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .testTag("add_tracker_button")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Goal or Habit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // --- PROGRESS CHART ---
            item {
                selectedTrackerForChart?.let { tracker ->
                    TrackerProgressChartCard(
                        tracker = tracker,
                        allLogs = logs
                    )
                } ?: run {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add habits/goals to view progress charts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // --- TRACKER SECTIONS ---
            item {
                Text(
                    text = "Habits & Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (trackers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrackChanges,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active goals or habits yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Press the + button to add one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                items(trackers) { tracker ->
                    val trackerLogsForThis = logs.filter { it.trackerId == tracker.id }
                    
                    // Habits check if done today, other goals build accumulated sums
                    val isHabit = tracker.type == "DAILY_HABIT"
                    val completionsToday = trackerLogsForThis.filter { it.logDate == todayDateString }.size
                    val isCheckedToday = completionsToday > 0

                    // Total completions for week/month/year counting
                    val totalCompletions = trackerLogsForThis.size

                    val isSelected = selectedTrackerForChart?.id == tracker.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTrackerForChart = tracker }
                            .testTag("tracker_item_${tracker.id}")
                    ) {
                        PaddingRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (tracker.type) {
                                        "DAILY_HABIT" -> Icons.Default.Cached
                                        "WEEKLY_GOAL" -> Icons.Default.DateRange
                                        "MONTHLY_GOAL" -> Icons.Default.CalendarMonth
                                        else -> Icons.Default.StarBorder
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tracker.type.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tracker.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Progress representation
                                val progressPercent = if (isHabit) {
                                    if (isCheckedToday) 1.0f else 0.0f
                                } else {
                                    val pct = totalCompletions.toFloat() / tracker.targetValue.toFloat()
                                    pct.coerceIn(0f, 1f)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { progressPercent },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (progressPercent >= 1f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (isHabit) {
                                            if (isCheckedToday) "Done Today" else "Not Done"
                                        } else {
                                            "$totalCompletions / ${tracker.targetValue}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Interactive controls
                            if (isHabit) {
                                IconToggleButton(
                                    checked = isCheckedToday,
                                    onCheckedChange = {
                                        viewModel.toggleHabitCheckIn(tracker.id, todayDateString, trackerLogsForThis)
                                    }
                                ) {
                                    val tint = if (isCheckedToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    Icon(
                                        if (isCheckedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Check habits today",
                                        tint = tint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                // Double button for count logging
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.decrementTrackerCheckIn(tracker.id, todayDateString, trackerLogsForThis)
                                        },
                                        enabled = totalCompletions > 0
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Decrement progress",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.incrementTrackerCheckIn(tracker.id, todayDateString)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.AddCircle,
                                            contentDescription = "Increment progress",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteTracker(tracker) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Tracker",
                                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTrackerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, type, target ->
                viewModel.addTracker(title, type, target)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TrackerProgressChartCard(
    tracker: Tracker,
    allLogs: List<TrackerLog>
) {
    val trackerLogs = allLogs.filter { it.trackerId == tracker.id }

    // Aggregate completion count for the past 7 days
    val datesLast7Days = remember {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val dayFormat = SimpleDateFormat("E", Locale.getDefault())
    val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val chartData = datesLast7Days.map { date ->
        val dateStr = dbDateFormat.format(date)
        val count = trackerLogs.filter { it.logDate == dateStr }.size
        Pair(dayFormat.format(date), count)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${tracker.title} — Activity Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tracked count over the past 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Chart rendering on Canvas
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingX = 40f
                val paddingY = 20f

                val usableWidth = width - (paddingX * 2)
                val usableHeight = height - (paddingY * 2)

                val stepX = usableWidth / 6f
                val maxVal = chartData.maxOf { it.second }.coerceAtLeast(3).toFloat()

                val points = chartData.mapIndexed { idx, pair ->
                    val x = paddingX + (idx * stepX)
                    val progressY = pair.second.toFloat() / maxVal
                    val y = height - paddingY - (progressY * usableHeight)
                    Offset(x, y)
                }

                // Draw Guide grids
                for (i in 0..3) {
                    val yVal = height - paddingY - ((i / 3f) * usableHeight)
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.15f),
                        start = Offset(paddingX, yVal),
                        end = Offset(width - paddingX, yVal),
                        strokeWidth = 1f
                    )
                }

                // Draw connecting path
                val connectionPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                }

                drawPath(
                    path = connectionPath,
                    color = primaryColor,
                    style = Stroke(width = 4f)
                )

                // Fill gradient under the path
                if (points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height - paddingY)
                        for (pt in points) {
                            lineTo(pt.x, pt.y)
                        }
                        lineTo(points.last().x, height - paddingY)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = points.minOf { it.y },
                            endY = height - paddingY
                        )
                    )
                }

                // Draw dots
                points.forEachIndexed { idx, pt ->
                    val value = chartData[idx].second
                    val circleColor = if (value > 0) secondaryColor else primaryColor
                    drawCircle(
                        color = circleColor,
                        radius = 8f,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = pt
                    )
                }
            }

            // Draw XLabels
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartData.forEach { data ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = data.first,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (data.second > 0) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data.second.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (data.second > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTrackerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("DAILY_HABIT") }
    var targetText by remember { mutableStateOf("1") }
    var titleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Goal Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.trim().isNotEmpty()) titleError = false
                    },
                    label = { Text("Title (e.g. Exercise, Read Books)") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Text(
                    text = "Tracker Timeline / Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val types = listOf(
                    Pair("DAILY_HABIT", "Daily Habit"),
                    Pair("WEEKLY_GOAL", "Weekly Goal"),
                    Pair("MONTHLY_GOAL", "Monthly Goal"),
                    Pair("YEARLY_GOAL", "Yearly Goal")
                )

                types.forEach { (typeVal, typeLabel) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { type = typeVal }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == typeVal,
                            onClick = { type = typeVal }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = typeLabel, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // If not Daily Habit, we configure target values
                if (type != "DAILY_HABIT") {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target count for accomplishment") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                titleError = true
                            } else {
                                val targetInt = targetText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                onAdd(title, type, if (type == "DAILY_HABIT") 1 else targetInt)
                            }
                        }
                    ) {
                        Text("Save Goal")
                    }
                }
            }
        }
    }
}

// Micro UI helper row to support clean touch expansion
@Composable
fun PaddingRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        content = content
    )
}
