package digital.tonima.myworkout.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        repository: WorkoutRepository,
    ) : ViewModel() {
        val sessions =
            repository.getAllSessions()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val masterExercises =
            repository.getAllMasterExercises()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
