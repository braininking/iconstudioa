package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.UiFeedbackEvent
import com.example.ui.navigation.AutoIconRoute
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.AutoIconStudioTheme
import com.example.ui.theme.GraphiteElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            AutoIconStudioTheme {
                AutoIconAppRoot()
            }
        }
    }
}

private data class NavTabItem(
    val route: AutoIconRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val mainTabs = listOf(
    NavTabItem(AutoIconRoute.Home, "Início", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavTabItem(AutoIconRoute.Editor, "Editor", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_editor"),
    NavTabItem(AutoIconRoute.Apps, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps, "nav_apps"),
    NavTabItem(AutoIconRoute.Themes, "Temas", Icons.Filled.Palette, Icons.Outlined.Palette, "nav_themes"),
    NavTabItem(AutoIconRoute.Automation, "Auto", Icons.Filled.AutoMode, Icons.Outlined.AutoMode, "nav_automation")
)

@Composable
fun AutoIconAppRoot(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiFeedbackEvent.Message -> {
                    snackbarHostState.showSnackbar(event.text)
                }
                is UiFeedbackEvent.ShareZipUri -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        putExtra(Intent.EXTRA_SUBJECT, "AutoIcon Studio - Pacote Nova Launcher")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Exportar Pacote Nova Launcher"))
                }
            }
        }
    }

    fun navigateToTab(route: AutoIconRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = ObsidianBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = GraphiteSurface,
                        contentColor = PureWhite,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        mainTabs.forEach { tab ->
                            val selected = currentDestination?.hasRoute(tab.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PureBlack,
                                    selectedTextColor = PureWhite,
                                    indicatorColor = PureWhite,
                                    unselectedIconColor = SilverSecondary,
                                    unselectedTextColor = SilverSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
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
                if (useRail) {
                    NavigationRail(
                        containerColor = GraphiteSurface,
                        contentColor = PureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        mainTabs.forEach { tab ->
                            val selected = currentDestination?.hasRoute(tab.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = PureBlack,
                                    selectedTextColor = PureWhite,
                                    indicatorColor = PureWhite,
                                    unselectedIconColor = SilverSecondary,
                                    unselectedTextColor = SilverSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = AutoIconRoute.Home
                    ) {
                        composable<AutoIconRoute.Home> {
                            HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenEditor = { navigateToTab(AutoIconRoute.Editor) },
                                onOpenApps = { navigateToTab(AutoIconRoute.Apps) },
                                onOpenAutomation = { navigateToTab(AutoIconRoute.Automation) }
                            )
                        }
                        composable<AutoIconRoute.Editor> {
                            EditorScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        composable<AutoIconRoute.Apps> {
                            AppsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        composable<AutoIconRoute.Themes> {
                            ThemesScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onEditTheme = { navigateToTab(AutoIconRoute.Editor) }
                            )
                        }
                        composable<AutoIconRoute.Automation> {
                            AutomationScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
