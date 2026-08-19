package digital.tonima.myworkout.ui.history

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.MasterExerciseEntity
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.ui.util.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HistoryState(
    val sessions: List<SessionWithLogs> = emptyList(),
    val masterExercises: List<MasterExerciseEntity> = emptyList(),
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
                            sessions = sessions,
                            masterExercises = masterExercises,
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
