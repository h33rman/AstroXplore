package com.example.astroxplore.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.astroxplore.features.auth.ui.LoginScreen
import com.example.astroxplore.features.auth.ui.SignupScreen
import com.example.astroxplore.features.feed.ui.FeedScreen
import com.example.astroxplore.features.onboarding.ui.OnboardingScreen
import com.example.astroxplore.features.profile.ui.LanguageScreen
import com.example.astroxplore.features.profile.ui.ProfileScreen
import com.example.astroxplore.features.search.ui.ExploreScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login,
        modifier = modifier
    ) {
        composable<Screen.Feed> {
            FeedScreen()
        }
        composable<Screen.Groups> {
            PlaceholderScreen("Groups")
        }
        composable<Screen.Explore> {
            ExploreScreen()
        }
        composable<Screen.Library> {
            PlaceholderScreen("Library")
        }
        composable<Screen.Profile> {
            ProfileScreen(onNavigateToLanguage = {
                navController.navigate(Screen.Language)
            })
        }
        composable<Screen.Language> {
            LanguageScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup)
                }
            )
        }
        composable<Screen.Signup> {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Onboarding) {
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
