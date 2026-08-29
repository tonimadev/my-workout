package digital.tonima.myworkout.features.stats.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.repository.GamificationRepository
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.ui.model.AchievementUiModel
import digital.tonima.myworkout.ui.model.GamificationStatsUiModel
import digital.tonima.myworkout.ui.model.LogUiModel
import digital.tonima.myworkout.ui.model.MasterExerciseUiModel
import digital.tonima.myworkout.ui.model.SessionUiModel
import digital.tonima.myworkout.ui.util.MviViewModel
import digital.tonima.myworkout.ui.util.toUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class StatsState(
    val masterExercises: ImmutableList<MasterExerciseUiModel> = persistentListOf(),
    val selectedExerciseId: Long? = null,
    val exerciseLogs: ImmutableList<LogUiModel> = persistentListOf(),
    val gamificationStats: GamificationStatsUiModel = GamificationStatsUiModel(0, 1, 0, 0L),
    val achievements: ImmutableList<AchievementUiModel> = persistentListOf(),
    val sessions: ImmutableList<SessionUiModel> = persistentListOf(),
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
    ) : MviViewModel<StatsState, StatsIntent>(StatsState()) {
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
                            masterExercises = masterExercises.map { it.toUiModel() }.toImmutableList(),
                            gamificationStats = stats.toUiModel(),
                            achievements = achievements.map { it.toUiModel() }.toImmutableList(),
                            sessions = sessions.map { it.toUiModel() }.toImmutableList(),
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
                updateState { copy(exerciseLogs = persistentListOf()) }
                return
            }
            logsJob =
                viewModelScope.launch {
                    repository.getLogsForMasterExercise(id).collect { logs ->
                        updateState { copy(exerciseLogs = logs.map { it.toUiModel() }.toImmutableList()) }
                    }
                }
        }
    }
