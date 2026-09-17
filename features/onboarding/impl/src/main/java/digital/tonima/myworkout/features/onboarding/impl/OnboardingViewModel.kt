package digital.tonima.myworkout.features.onboarding.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.catalog.WorkoutTemplate
import digital.tonima.myworkout.data.preferences.UserPreferencesRepository
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val workoutRepository: WorkoutRepository,
    ) : ViewModel() {
        val onboardingCompleted: StateFlow<Boolean> =
            userPreferencesRepository.onboardingCompleted
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        fun completeOnboarding() {
            viewModelScope.launch {
                userPreferencesRepository.setOnboardingCompleted(true)
            }
        }

        /** Creates a workout for each selected template. Safe to call with an empty set (no-op). */
        fun applyTemplates(templates: Set<WorkoutTemplate>) {
            viewModelScope.launch {
                templates.forEach { template -> workoutRepository.applyWorkoutTemplate(template) }
            }
        }
    }
