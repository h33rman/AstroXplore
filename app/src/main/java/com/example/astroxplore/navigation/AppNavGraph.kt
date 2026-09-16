package com.example.astroxplore.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.astroxplore.features.auth.ui.LoginScreen
import com.example.astroxplore.features.auth.ui.SignupScreen
import com.example.astroxplore.features.feed.ui.FeedScreen
import com.example.astroxplore.features.onboarding.ui.OnboardingScreen
import com.example.astroxplore.features.profile.ui.InterestsScreen
import com.example.astroxplore.features.profile.ui.LanguageScreen
import com.example.astroxplore.features.profile.ui.ProfileScreen
import com.example.astroxplore.features.search.ui.ExploreScreen
import com.example.astroxplore.features.splash.ui.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Feed> {
            FeedScreen(
                onSearchClick = {
                    navController.navigate(Screen.Explore(autofocus = true))
                }
            )
        }
        composable<Screen.Groups> {
            PlaceholderScreen("Groups")
        }
        composable<Screen.Explore> { backStackEntry ->
            val explore = backStackEntry.toRoute<Screen.Explore>()
            ExploreScreen(autofocus = explore.autofocus)
        }
        composable<Screen.Library> {
            PlaceholderScreen("Library")
        }
        composable<Screen.Profile> {
            ProfileScreen(
                onNavigateToLanguage = {
                    navController.navigate(Screen.Language)
                },
                onNavigateToInterests = {
                    navController.navigate(Screen.Interests)
                }
            )
        }
        composable<Screen.Language> {
            LanguageScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable<Screen.Interests> {
            InterestsScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable<Screen.Login>(
            enterTransition = { fadeIn(tween(500)) + slideInHorizontally { it / 2 } },
            exitTransition = { fadeOut(tween(500)) + slideOutHorizontally { -it / 2 } }
        ) {
            LoginScreen(
                onLoginSuccess = { isOnboarded ->
                    val destination = if (isOnboarded) Screen.Feed else Screen.Onboarding
                    navController.navigate(destination) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup)
                }
            )
        }
        composable<Screen.Signup>(
            enterTransition = { fadeIn(tween(500)) + slideInHorizontally { it / 2 } },
            exitTransition = { fadeOut(tween(500)) + slideOutHorizontally { -it / 2 } }
        ) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Feed) {
                        popUpTo(Screen.Signup) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Signup) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Onboarding> {
            OnboardingScreen(onOnboardingComplete = {
                navController.navigate(Screen.Feed) {
                    popUpTo(Screen.Onboarding) { inclusive = true }
                }
            })
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$name Screen Coming Soon")
    }
}
