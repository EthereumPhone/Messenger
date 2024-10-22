package org.ethereumphone.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ethereumphone.onboarding.OnboardingRoute
import org.ethereumphone.onboarding.OnboardingScreen


const val onboardingRoute = "onboarding_route"
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(onboardingRoute, navOptions)
}


fun NavGraphBuilder.onboardingScreen(
    onSkipOnboarding: () -> Unit
) {
    composable(
        route = onboardingRoute
    ) {
        OnboardingRoute(onSkipOnboarding = onSkipOnboarding)
    }
}