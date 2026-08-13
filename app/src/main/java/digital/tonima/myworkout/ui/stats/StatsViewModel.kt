package digital.tonima.myworkout.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
    ) : ViewModel() {
        val masterExercises =
            repository.getAllMasterExercises()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        private val _selectedExerciseId = MutableStateFlow<Long?>(null)
        val selectedExerciseId = _selectedExerciseId.asStateFlow()

        val exerciseLogs =
            _selectedExerciseId.flatMapLatest { id ->
                if (id != null) {
                    repository.getLogsForMasterExercise(id)
                } else {
                    flowOf(emptyList())
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fun selectExercise(id: Long?) {
            _selectedExerciseId.value = id
        }
    }
