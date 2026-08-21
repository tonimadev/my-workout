package digital.tonima.myworkout.features.onboarding.bridge

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingDestination : NavKey {
    @Serializable
    data object Onboarding : OnboardingDestination
}
