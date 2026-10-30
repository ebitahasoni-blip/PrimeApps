package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EssayViewModel
import com.example.ui.components.AboutDialog
import com.example.ui.screens.InputScreen
import com.example.ui.screens.ProfileDashboardScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.theme.PrimeEssayTheme

class MainActivity : ComponentActivity() {

    private val viewModel: EssayViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrimeEssayTheme {
                var currentTab by remember { mutableIntStateOf(0) }
                var showAboutDialog by remember { mutableStateOf(false) }

                val currentReport by viewModel.currentReport.collectAsState()
                val isOnline by viewModel.isOnline.collectAsState()
                val unsyncedCount by viewModel.unsyncedCount.collectAsState()

                if (showAboutDialog) {
                    AboutDialog(onDismiss = { showAboutDialog = false })
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 840.dp

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row {
                                        Text(
                                            text = "Prime Essay Feedback",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                },
                                actions = {
                                    // Offline / Online indicator
                                    IconButton(
                                        onClick = { /* Informative icon */ },
                                        modifier = Modifier.testTag("connectivity_status_indicator")
                                    ) {
                                        if (unsyncedCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge { Text("$unsyncedCount") }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                                    contentDescription = if (isOnline) "Online" else "Offline",
                                                    tint = if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                                contentDescription = if (isOnline) "Online" else "Offline",
                                                tint = if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { showAboutDialog = true },
                                        modifier = Modifier.testTag("about_app_button")
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "About Prime Essay Feedback")
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            if (!isExpanded) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.testTag("mobile_bottom_nav")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == 0 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 0
                                        },
                                        icon = { Icon(Icons.Default.Create, contentDescription = "Evaluate") },
                                        label = { Text("Evaluate", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_evaluate")
                                    )
                                    NavigationBarItem(
                                        selected = currentReport != null,
                                        onClick = {
                                            if (currentReport != null) {
                                                currentTab = 1
                                            }
                                        },
                                        enabled = currentReport != null,
                                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                                        label = { Text("Report", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_report")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 2 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 2
                                        },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_dashboard")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 3 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 3
                                        },
                                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Resources") },
                                        label = { Text("Resources", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_resources")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isExpanded) {
                                NavigationRail(
                                    modifier = Modifier.testTag("tablet_navigation_rail")
                                ) {
                                    Spacer(modifier = Modifier.size(16.dp))
                                    NavigationRailItem(
                                        selected = currentTab == 0 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 0
                                        },
                                        icon = { Icon(Icons.Default.Create, contentDescription = "Evaluate") },
                                        label = { Text("Evaluate") },
                                        modifier = Modifier.testTag("rail_tab_evaluate")
                                    )
                                    NavigationRailItem(
                                        selected = currentReport != null,
                                        onClick = {
                                            if (currentReport != null) {
                                                currentTab = 1
                                            }
                                        },
                                        enabled = currentReport != null,
                                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                                        label = { Text("Report") },
                                        modifier = Modifier.testTag("rail_tab_report")
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == 2 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 2
                                        },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard") },
                                        modifier = Modifier.testTag("rail_tab_dashboard")
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == 3 && currentReport == null,
                                        onClick = {
                                            viewModel.closeReport()
                                            currentTab = 3
                                        },
                                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Resources") },
                                        label = { Text("Resources") },
                                        modifier = Modifier.testTag("rail_tab_resources")
                                    )
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (currentReport != null) {
                                    ReportScreen(
                                        report = currentReport!!,
                                        viewModel = viewModel,
                                        onBack = { viewModel.closeReport() }
                                    )
                                } else {
                                    when (currentTab) {
                                        0 -> InputScreen(viewModel = viewModel)
                                        2 -> ProfileDashboardScreen(
                                            viewModel = viewModel,
                                            onOpenReport = { report ->
                                                viewModel.viewReport(report)
                                            }
                                        )
                                        3 -> ResourcesScreen(viewModel = viewModel)
                                        else -> InputScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
