package com.example.astroxplore

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.astroxplore.core.database.ThemeMode
import com.example.astroxplore.features.profile.ui.ProfileViewModel
import com.example.astroxplore.navigation.AppNavGraph
import com.example.astroxplore.navigation.Screen
import com.example.astroxplore.ui.theme.AstroXploreTheme
import io.github.jan.supabase.auth.status.SessionStatus
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
            val mainViewModel: MainViewModel = hiltViewModel()
            val sessionStatus by mainViewModel.sessionStatus.collectAsState()

            val profileViewModel: ProfileViewModel = hiltViewModel()
            val settingsState by profileViewModel.uiState.collectAsState()

            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Handle Language change
            val locale = Locale.forLanguageTag(settingsState.language)
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
                    val isOnboarded by mainViewModel.isOnboarded.collectAsState()
                    AstroXploreMain(sessionStatus, isOnboarded)
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AstroXploreMain(
    sessionStatus: SessionStatus = SessionStatus.Initializing,
    isOnboarded: Boolean? = null,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(sessionStatus, isOnboarded) {
        val currentRoute = currentDestination?.route
        val isOnSplash = currentRoute?.contains("Splash") == true
        val isOnAuth = currentRoute?.contains("Login") == true || currentRoute?.contains("Signup") == true

        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                if (isOnboarded == true) {
                    if (isOnSplash || isOnAuth) {
                        navController.navigate(Screen.Feed) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else if (isOnboarded == false) {
                    if (isOnSplash || isOnAuth) {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                if (!isOnAuth) {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {} 
        }
    }

    val showNavigation = currentDestination != null && (
            currentDestination.hasRoute<Screen.Feed>() ||
            currentDestination.hasRoute<Screen.Groups>() ||
            currentDestination.hasRoute<Screen.Explore>() ||
            currentDestination.hasRoute<Screen.Library>() ||
            currentDestination.hasRoute<Screen.Profile>()
    )

    if (showNavigation) {
        val navSuiteItemColors = NavigationSuiteDefaults.itemColors(
            navigationBarItemColors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            ),
            navigationRailItemColors = NavigationRailItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
            )
        )

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
                    val isSelected = currentDestination.hasRoute(destination.screen::class)
                    item(
                        icon = {
                            AnimatedContent(
                                targetState = isSelected,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + 
                                            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                                        .togetherWith(fadeOut(animationSpec = tween(90)))
                                },
                                label = "icon_transition"
                            ) { selected ->
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (isSelected && destination == AppDestinations.FEED) {
                                mainViewModel.triggerScrollToTop()
                            } else {
                                navController.navigate(destination.screen) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = navSuiteItemColors
                    )
                }
            }
        ) {
            AppNavGraph(navController = navController)
        }
    } else {
        AppNavGraph(navController = navController)
    }
}

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
) {
    FEED("Feed", Icons.Filled.Newspaper, Icons.Outlined.Newspaper, Screen.Feed),
    GROUPS("Groups", Icons.Filled.Groups, Icons.Outlined.Groups, Screen.Groups),
    EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore, Screen.Explore()),
    LIBRARY("Library", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, Screen.Library),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, Screen.Profile),
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
