package digital.tonima.myworkout.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val alertManager: AlertManager,
    ) : ViewModel() {
        val workouts =
            repository.getAllWorkouts()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        private val _activeSession = MutableStateFlow<SessionWithLogs?>(null)
        val activeSession: StateFlow<SessionWithLogs?> = _activeSession.asStateFlow()

        private val _currentWorkout = MutableStateFlow<WorkoutWithExercises?>(null)
        val currentWorkout: StateFlow<WorkoutWithExercises?> = _currentWorkout.asStateFlow()

        private val _restTimeRemaining = MutableStateFlow(0L)
        val restTimeRemaining: StateFlow<Long> = _restTimeRemaining.asStateFlow()

        fun loadWorkout(workoutId: Long) {
            viewModelScope.launch {
                repository.getWorkoutById(workoutId).collect { workout ->
                    _currentWorkout.value = workout
                }
            }
        }

        fun startSession(workoutId: Long) {
            viewModelScope.launch {
                val sessionId = repository.startSession(workoutId)
                repository.getSessionById(sessionId).collect { session ->
                    _activeSession.value = session
                }
            }
        }

        fun completeSet(
            exerciseId: Long,
            setId: Long,
            weight: Float,
            reps: Int,
            restInterval: Int,
        ) {
            val session = _activeSession.value ?: return
            viewModelScope.launch {
                val log =
                    WorkoutLogEntity(
                        sessionId = session.session.id,
                        exerciseId = exerciseId,
                        setId = setId,
                        actualWeight = weight.toDouble(),
                        actualReps = reps,
                        timestamp = System.currentTimeMillis(),
                    )
                repository.addLog(log)
                startRestTimer(restInterval)
            }
        }

        private fun startRestTimer(seconds: Int) {
            viewModelScope.launch {
                _restTimeRemaining.value = seconds.toLong()
                while (_restTimeRemaining.value > 0) {
                    kotlinx.coroutines.delay(1000)
                    _restTimeRemaining.value -= 1
                }
                alertManager.triggerCompletionAlert()
            }
        }

        fun finishSession() {
            val session = _activeSession.value ?: return
            viewModelScope.launch {
                repository.finishSession(session.session)
                _activeSession.value = null
                _currentWorkout.value = null
            }
        }
    }
