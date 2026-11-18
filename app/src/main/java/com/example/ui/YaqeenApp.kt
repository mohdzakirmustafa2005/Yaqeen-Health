package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.SubscriptionPlan
import com.example.domain.UserRole
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.stringsFor
import com.example.ui.screens.AlertsEngineScreen
import com.example.ui.screens.ConsoleSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PopulationScreen
import com.example.ui.screens.WorkerCheckInScreen

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_CHECKIN = "worker_checkin"
const val ROUTE_ALERTS = "alerts_engine"
const val ROUTE_POPULATION = "population_intelligence"
const val ROUTE_CONSOLE = "console_settings"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YaqeenApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language = uiState.preferences.language
    val strings = remember(language) { stringsFor(language) }
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val snackbarHostState = remember { SnackbarHostState() }
    var showRegisterOrgDialog by remember { mutableStateOf(false) }
    var showJudgeDemoGuide by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val openAlertCount = uiState.orgAlerts.count { it.actionStatus == "OPEN" }
    val navItems = listOf(
        NavDestinationItem(
            route = ROUTE_DASHBOARD,
            label = strings.navDashboard,
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            testTag = "nav_dashboard"
        ),
        NavDestinationItem(
            route = ROUTE_CHECKIN,
            label = strings.navCheckIn,
            selectedIcon = Icons.Filled.HealthAndSafety,
            unselectedIcon = Icons.Outlined.HealthAndSafety,
            testTag = "nav_checkin",
            badgeCount = uiState.pendingSyncCount
        ),
        NavDestinationItem(
            route = ROUTE_ALERTS,
            label = strings.navAlerts,
            selectedIcon = Icons.Filled.NotificationsActive,
            unselectedIcon = Icons.Outlined.NotificationsActive,
            testTag = "nav_alerts",
            badgeCount = openAlertCount
        ),
        NavDestinationItem(
            route = ROUTE_POPULATION,
            label = strings.navPopulation,
            selectedIcon = Icons.Filled.Public,
            unselectedIcon = Icons.Outlined.Public,
            testTag = "nav_population"
        ),
        NavDestinationItem(
            route = ROUTE_CONSOLE,
            label = strings.navConsole,
            selectedIcon = Icons.Filled.AdminPanelSettings,
            unselectedIcon = Icons.Outlined.AdminPanelSettings,
            testTag = "nav_console"
        )
    )

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        if (!uiState.preferences.isSignedIn) {
            LoginScreen(
                selectedLanguage = language,
                onSelectLanguage = viewModel::setLanguage,
                onLogin = viewModel::signInWithDemoCredentials
            )
            return@CompositionLocalProvider
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 700.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        title = {
                            Column {
                                Text(
                                    text = strings.brandTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = uiState.activeOrganization?.name ?: strings.brandTagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            // Role Switcher Pill
                            Box {
                                Surface(
                                    onClick = { showRoleMenu = true },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("topbar_role_switcher")
                                ) {
                                    Text(
                                        text = uiState.preferences.activeRole.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false }
                                ) {
                                    UserRole.entries.forEach { role ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(role.displayName, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        role.scopeDescription,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.setActiveRole(role)
                                                showRoleMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(4.dp))
                            // Language Switcher IconButton
                            Box {
                                IconButton(
                                    onClick = { showLanguageMenu = true },
                                    modifier = Modifier.testTag("topbar_language_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = strings.languageLabel,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    onDismissRequest = { showLanguageMenu = false }
                                ) {
                                    AppLanguage.entries.forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text("${lang.nativeLabel} (${lang.code.uppercase()})") },
                                            onClick = {
                                                viewModel.setLanguage(lang)
                                                showLanguageMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { showJudgeDemoGuide = true },
                                modifier = Modifier.testTag("topbar_judge_guide_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = "5-Minute Competition Demo Guide",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            IconButton(
                                onClick = viewModel::signOut,
                                modifier = Modifier.testTag("topbar_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!useNavRail) {
                        NavigationBar {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (item.badgeCount > 0) {
                                                    Badge { Text(item.badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (item.badgeCount > 0) {
                                                    Badge { Text(item.badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    },
                                    label = { Text(item.label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_DASHBOARD,
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            composable(ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    uiState = uiState,
                                    onSimulateReading = { branch, temp, rh, wbgt, aqi, source ->
                                        viewModel.recordEnvironmentalReading(branch, temp, rh, wbgt, aqi, source)
                                    },
                                    onSyncQueue = viewModel::syncOfflineQueue,
                                    onNavigateToAlerts = { navigateTo(ROUTE_ALERTS) },
                                    onNavigateToCheckIn = { navigateTo(ROUTE_CHECKIN) },
                                    onOpenOrgRegistration = { showRegisterOrgDialog = true }
                                )
                            }
                            composable(ROUTE_CHECKIN) {
                                WorkerCheckInScreen(
                                    uiState = uiState,
                                    onToggleOfflineMode = viewModel::setOfflineMode,
                                    onSyncQueue = viewModel::syncOfflineQueue,
                                    onSubmitCheckIn = viewModel::submitCheckIn
                                )
                            }
                            composable(ROUTE_ALERTS) {
                                AlertsEngineScreen(
                                    uiState = uiState,
                                    onReviewAlert = viewModel::reviewAlert
                                )
                            }
                            composable(ROUTE_POPULATION) {
                                PopulationScreen(
                                    uiState = uiState,
                                    onGenerateAiBriefing = viewModel::generateAiBriefing,
                                    onBuildCsvReport = viewModel::buildDeIdentifiedCsvReport
                                )
                            }
                            composable(ROUTE_CONSOLE) {
                                ConsoleSettingsScreen(
                                    uiState = uiState,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onSelectRole = viewModel::setActiveRole,
                                    onSelectOrg = viewModel::setActiveOrganization,
                                    onUpdateConsent = viewModel::updateWorkerConsent,
                                    onDeleteWorkerData = viewModel::deleteWorkerCheckIns,
                                    onToggleTenantSuspension = viewModel::toggleTenantSuspension,
                                    onUpdateSubscriptionPlan = viewModel::updateSubscriptionPlan,
                                    onOpenOrgRegistration = { showRegisterOrgDialog = true },
                                    onAddBranch = viewModel::addBranch
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegisterOrgDialog) {
        RegisterOrganizationDialog(
            onDismiss = { showRegisterOrgDialog = false },
            onRegister = { name, industry, city, plan, branch, workers ->
                viewModel.registerOrganization(name, industry, city, plan, branch, workers)
                showRegisterOrgDialog = false
            }
        )
    }

    if (showJudgeDemoGuide) {
        AlertDialog(
            onDismissRequest = { showJudgeDemoGuide = false },
            title = { Text("5-Minute Competition Demo Guide") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Organization Overview: Inspect Deccan Precision Mfg (Mahbubnagar) simulated KPIs & role-scoped privacy.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Worker Check-In: Open Check-In tab, test Offline Mode queue + Sync, and submit fatigue/symptoms with DPDP consent.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Early Warning Engine: Tap 'Heatwave 39°C' or 'AQI Dust 245' preset to generate an explainable alert (with WBGT & missing-sensor checks).", style = MaterialTheme.typography.bodySmall)
                    Text("4. Employer Response: Open Alerts tab, tap Safety Review & Action, and log a preventive action to write an audit record.", style = MaterialTheme.typography.bodySmall)
                    Text("5. Population & Multilingual: Open Population tab for the Telangana risk map & CSV export; switch EN / हिन्दी / తెలుగు / اردو anytime.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showJudgeDemoGuide = false }) { Text("Got It") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegisterOrganizationDialog(
    onDismiss: () -> Unit,
    onRegister: (String, String, String, SubscriptionPlan, String, Int) -> Unit
) {
    var orgName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("Factory & Manufacturing") }
    var city by remember { mutableStateOf("Hyderabad, TS") }
    var branchName by remember { mutableStateOf("Main Plant · Unit 1") }
    var workersText by remember { mutableStateOf("60") }
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.FREE_PILOT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Organization (Multi-Tenant)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("Organization Name") },
                    placeholder = { Text("e.g., Nizamabad Steel Works") },
                    singleLine = true,
                    modifier = Modifier.testTag("register_org_name_input")
                )
                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Industry Sector") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City / State") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Initial Workplace Branch") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = workersText,
                    onValueChange = { workersText = it },
                    label = { Text("Estimated Workers") },
                    singleLine = true
                )
                Text("Subscription Plan", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        SubscriptionPlan.FREE_PILOT,
                        SubscriptionPlan.STARTER,
                        SubscriptionPlan.PROFESSIONAL
                    ).forEach { plan ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = { selectedPlan = plan },
                            label = { Text("${plan.label} (${plan.priceInr})") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (orgName.isNotBlank()) {
                        onRegister(
                            orgName,
                            industry,
                            city,
                            selectedPlan,
                            branchName.ifBlank { "Main Site" },
                            workersText.toIntOrNull() ?: 30
                        )
                    }
                },
                enabled = orgName.isNotBlank(),
                modifier = Modifier.testTag("confirm_register_org_button")
            ) {
                Text("Create Tenant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
