package digital.tonima.myworkout.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.repository.GamificationRepository
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        gamificationRepository: GamificationRepository,
    ) : ViewModel() {
        val masterExercises =
            repository.getAllMasterExercises()
                .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

        val gamificationStats =
            gamificationRepository.getGamificationStats()
                .stateIn(viewModelScope, WhileSubscribed(5000), null)

        val achievements =
            gamificationRepository.getAchievements()
                .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

        val sessions =
            repository.getAllSessions()
                .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

        private val _selectedExerciseId = MutableStateFlow<Long?>(null)
        val selectedExerciseId = _selectedExerciseId.asStateFlow()

        val exerciseLogs =
            _selectedExerciseId.flatMapLatest { id ->
                if (id != null) {
                    repository.getLogsForMasterExercise(id)
                } else {
                    flowOf(emptyList())
                }
            }.stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

        fun selectExercise(id: Long?) {
            _selectedExerciseId.value = id
        }
    }
