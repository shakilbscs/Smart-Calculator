package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CalculatorCategory
import com.example.model.CalculatorId
import com.example.ui.components.ResultCopiedDialog
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.CalculatorDetailScreen
import com.example.ui.screens.DrawerMenu
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

sealed class AppScreen {
    data object Onboarding : AppScreen()
    data object Home : AppScreen()
    data class CategoryDetail(val category: CalculatorCategory) : AppScreen()
    data class CalculatorDetail(val calculatorId: CalculatorId) : AppScreen()
    data object Favorites : AppScreen()
    data object History : AppScreen()
    data object Settings : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppRoot(
                    viewModel = mainViewModel,
                    initialOnboardingDone = onboardingCompleted,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    viewModel: MainViewModel,
    initialOnboardingDone: Boolean,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember {
        mutableStateOf<AppScreen>(
            if (initialOnboardingDone) AppScreen.Home else AppScreen.Onboarding
        )
    }

    val copyModalMsg by viewModel.copyModalMessage.collectAsState()

    // Handle system back button properly across custom screen states
    BackHandler(enabled = currentScreen !is AppScreen.Home && currentScreen !is AppScreen.Onboarding) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.Home
        }
    }

    if (currentScreen is AppScreen.Onboarding) {
        OnboardingScreen(
            onFinish = {
                viewModel.completeOnboarding()
                currentScreen = AppScreen.Home
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerMenu(
                    selectedRoute = when (currentScreen) {
                        is AppScreen.Home -> "home"
                        is AppScreen.Favorites -> "favorites"
                        is AppScreen.History -> "history"
                        is AppScreen.Settings -> "settings"
                        else -> "home"
                    },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        viewModel.setThemeMode(if (isDarkTheme) "light" else "dark")
                    },
                    onNavigate = { route ->
                        currentScreen = when (route) {
                            "home" -> AppScreen.Home
                            "favorites" -> AppScreen.Favorites
                            "history" -> AppScreen.History
                            "settings" -> AppScreen.Settings
                            else -> AppScreen.Home
                        }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        val showBottomBar = currentScreen is AppScreen.Home ||
                currentScreen is AppScreen.Favorites ||
                currentScreen is AppScreen.History ||
                currentScreen is AppScreen.Settings

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Home,
                            onClick = { currentScreen = AppScreen.Home },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Favorites,
                            onClick = { currentScreen = AppScreen.Favorites },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.History,
                            onClick = { currentScreen = AppScreen.History },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is AppScreen.Settings,
                            onClick = { currentScreen = AppScreen.Settings },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = currentScreen) {
                    is AppScreen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSelectCalculator = { calcId ->
                                viewModel.onCalculatorOpened(calcId)
                                currentScreen = AppScreen.CalculatorDetail(calcId)
                            },
                            onSelectCategory = { cat ->
                                currentScreen = AppScreen.CategoryDetail(cat)
                            }
                        )
                    }
                    is AppScreen.CategoryDetail -> {
                        CategoryScreen(
                            category = screen.category,
                            onBack = { currentScreen = AppScreen.Home },
                            onSelectCalculator = { calcId ->
                                viewModel.onCalculatorOpened(calcId)
                                currentScreen = AppScreen.CalculatorDetail(calcId)
                            }
                        )
                    }
                    is AppScreen.CalculatorDetail -> {
                        CalculatorDetailScreen(
                            calculatorId = screen.calculatorId,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                    is AppScreen.Favorites -> {
                        FavoritesScreen(
                            viewModel = viewModel,
                            onSelectCalculator = { calcId ->
                                viewModel.onCalculatorOpened(calcId)
                                currentScreen = AppScreen.CalculatorDetail(calcId)
                            }
                        )
                    }
                    is AppScreen.History -> {
                        HistoryScreen(
                            viewModel = viewModel
                        )
                    }
                    is AppScreen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                    is AppScreen.Onboarding -> {}
                }
            }
        }
    }

    // Modal result action popup (Matches Screen 24 in showcase)
    copyModalMsg?.let { msg ->
        ResultCopiedDialog(
            message = msg,
            onDismiss = { viewModel.dismissCopyModal() },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, msg)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Result"))
            }
        )
    }
}
