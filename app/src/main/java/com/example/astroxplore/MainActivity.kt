package com.example.astroxplore

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.astroxplore.core.database.ThemeMode
import com.example.astroxplore.features.profile.ui.SettingsViewModel
import com.example.astroxplore.navigation.AppNavGraph
import com.example.astroxplore.navigation.Screen
import com.example.astroxplore.ui.theme.AstroXploreTheme
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Handle Language change
            val locale = Locale(settingsState.language)
            val configuration = LocalConfiguration.current
            val context = LocalContext.current
            
            val localizedContext = remember(locale) {
                val config = Configuration(configuration)
                config.setLocale(locale)
                val contextWithConfig = context.createConfigurationContext(config)
                object : ContextWrapper(context) {
                    override fun getResources(): Resources = contextWithConfig.resources
                }
            }
            
            CompositionLocalProvider(
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalContext provides localizedContext
            ) {
                AstroXploreTheme(
                    darkTheme = darkTheme,
                    dynamicColor = settingsState.dynamicColorEnabled
                ) {
                    AstroXploreMain()
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AstroXploreMain() {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.FEED) }

    NavigationSuiteScaffold(
        layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfo()
        ),
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = {
                        currentDestination = destination
                        when (destination) {
                            AppDestinations.FEED -> navController.navigate(Screen.Feed)
                            AppDestinations.GROUPS -> navController.navigate(Screen.Groups)
                            AppDestinations.EXPLORE -> navController.navigate(Screen.Explore)
                            AppDestinations.LIBRARY -> navController.navigate(Screen.Library)
                            AppDestinations.PROFILE -> navController.navigate(Screen.Profile)
                        }
                    }
                )
            }
        }
    ) {
        AppNavGraph(navController = navController)
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    FEED("Feed", Icons.Default.Newspaper),
    GROUPS("Groups", Icons.Default.Groups),
    EXPLORE("Explore", Icons.Default.Explore),
    LIBRARY("Library", Icons.Default.Bookmark),
    PROFILE("Profile", Icons.Default.Person),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AstroXploreTheme {
        Greeting("Android")
    }
}