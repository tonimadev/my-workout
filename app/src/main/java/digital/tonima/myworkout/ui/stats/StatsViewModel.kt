package digital.tonima.myworkout.ui.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.preferences.GamificationStats
import digital.tonima.myworkout.data.repository.GamificationRepository
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.ui.util.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class StatsState(
    val masterExercises: List<MasterExerciseEntity> = emptyList(),
    val selectedExerciseId: Long? = null,
    val exerciseLogs: List<WorkoutLogEntity> = emptyList(),
    val gamificationStats: GamificationStats = GamificationStats(0, 1, 0, 0L),
    val achievements: List<AchievementEntity> = emptyList(),
    val sessions: List<SessionWithLogs> = emptyList(),
)

sealed interface StatsIntent {
    data class SelectExercise(val id: Long?) : StatsIntent
}

@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val gamificationRepository: GamificationRepository,
    ) : MviViewModel<StatsState, StatsIntent, Unit>(StatsState()) {
        private var logsJob: Job? = null

        init {
            observeBaseData()
        }

        private fun observeBaseData() {
            viewModelScope.launch {
                combine(
                    repository.getAllMasterExercises(),
                    gamificationRepository.getGamificationStats(),
                    gamificationRepository.getAchievements(),
                    repository.getAllSessions(),
                ) { masterExercises, stats, achievements, sessions ->
                    updateState {
                        copy(
                            masterExercises = masterExercises,
                            gamificationStats = stats,
                            achievements = achievements,
                            sessions = sessions,
                        )
                    }
                }.collect {}
            }
        }

        override fun handleIntent(intent: StatsIntent) {
            when (intent) {
                is StatsIntent.SelectExercise -> selectExercise(intent.id)
            }
        }

        private fun selectExercise(id: Long?) {
            updateState { copy(selectedExerciseId = id) }
            observeExerciseLogs(id)
        }

        private fun observeExerciseLogs(id: Long?) {
            logsJob?.cancel()
            if (id == null) {
                updateState { copy(exerciseLogs = emptyList()) }
                return
            }
            logsJob =
                viewModelScope.launch {
                    repository.getLogsForMasterExercise(id).collect { logs ->
                        updateState { copy(exerciseLogs = logs) }
                    }
                }
        }
    }
