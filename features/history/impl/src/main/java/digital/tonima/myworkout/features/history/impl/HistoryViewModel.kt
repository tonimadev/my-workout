package digital.tonima.myworkout.features.history.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.ui.model.MasterExerciseUiModel
import digital.tonima.myworkout.ui.model.SessionUiModel
import digital.tonima.myworkout.ui.util.MviViewModel
import digital.tonima.myworkout.ui.util.toUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HistoryState(
    val sessions: ImmutableList<SessionUiModel> = persistentListOf(),
    val masterExercises: ImmutableList<MasterExerciseUiModel> = persistentListOf(),
)

sealed interface HistoryIntent {
    data object Refresh : HistoryIntent
}

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
    ) : MviViewModel<HistoryState, HistoryIntent>(HistoryState()) {
        init {
            observeHistory()
        }

        private fun observeHistory() {
            viewModelScope.launch {
                combine(
                    repository.getAllSessions(),
                    repository.getAllMasterExercises(),
                ) { sessions, masterExercises ->
                    updateState {
                        copy(
                            sessions = sessions.map { it.toUiModel() }.toImmutableList(),
                            masterExercises = masterExercises.map { it.toUiModel() }.toImmutableList(),
                        )
                    }
                }.collect {}
            }
        }

        override fun handleIntent(intent: HistoryIntent) {
            when (intent) {
                is HistoryIntent.Refresh -> observeHistory()
            }
        }
    }
