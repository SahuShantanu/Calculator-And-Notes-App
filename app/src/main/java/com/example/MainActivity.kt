package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.TodoScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val isDark = viewModel.isDarkMode

            MyApplicationTheme(darkTheme = isDark) {
                var currentTab by remember { mutableStateOf("Goals") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "OmniTask",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            actions = {
                                // SLEEK THEME MODE MANUAL TOGGLER
                                IconButton(
                                    onClick = { viewModel.isDarkMode = !viewModel.isDarkMode },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                        contentDescription = "Toggle dark mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_navigation_bar")
                        ) {
                            val tabs = listOf(
                                Triple("Goals", Icons.Filled.TrackChanges, "Goals & Habits"),
                                Triple("Tasks", Icons.Filled.Checklist, "To-Do List"),
                                Triple("Notes", Icons.Filled.NoteAlt, "Notes"),
                                Triple("Calc", Icons.Filled.Calculate, "Calculator")
                            )

                            tabs.forEach { (tabId, icon, label) ->
                                val isSelected = currentTab == tabId
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tabId },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tabId,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                            }
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    
                    when (currentTab) {
                        "Goals" -> DashboardScreen(viewModel = viewModel, modifier = screenModifier)
                        "Tasks" -> TodoScreen(viewModel = viewModel, modifier = screenModifier)
                        "Notes" -> NotesScreen(viewModel = viewModel, modifier = screenModifier)
                        "Calc" -> CalculatorScreen(viewModel = viewModel, modifier = screenModifier)
                    }
                }
            }
        }
    }
}
