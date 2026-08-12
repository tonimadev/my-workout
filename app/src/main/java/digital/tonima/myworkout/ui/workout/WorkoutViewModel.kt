package digital.tonima.myworkout.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val workouts = repository.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
    val uiState: StateFlow<WorkoutUiState> = _uiState

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun addWorkout(name: String) {
        viewModelScope.launch {
            repository.addWorkout(WorkoutEntity(name = name), emptyList())
        }
    }

    // Tracking state
    private val _activeSession = MutableStateFlow<SessionWithLogs?>(null)
    val activeSession: StateFlow<SessionWithLogs?> = _activeSession

    fun startWorkout(workoutId: Long) {
        viewModelScope.launch {
            val sessionId = repository.startSession(workoutId)
            repository.getSessionById(sessionId).collect { session ->
                _activeSession.value = session
            }
        }
    }

    fun logSet(sessionId: Long, exerciseId: Long, setId: Long, weight: Double, reps: Int) {
        viewModelScope.launch {
            repository.addLog(
                WorkoutLogEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setId = setId,
                    actualWeight = weight,
                    actualReps = reps,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            val session = _activeSession.value?.session
            if (session != null) {
                repository.finishSession(session)
                _activeSession.value = null
            }
        }
    }
    
    fun getWorkout(id: Long) = repository.getWorkoutById(id)

    fun addExercise(workoutId: Long, name: String) {
        viewModelScope.launch {
            val workoutWithEx = repository.getWorkoutById(workoutId).first()
            if (workoutWithEx != null) {
                val newExercise = ExerciseEntity(
                    workoutId = workoutId,
                    name = name,
                    order = workoutWithEx.exercises.size
                )
                repository.addWorkout(
                    workoutWithEx.workout,
                    workoutWithEx.exercises + ExerciseWithSets(newExercise, emptyList())
                )
            }
        }
    }

    fun updateSet(workoutId: Long, exerciseId: Long, setId: Long, weight: Double, reps: Int, rest: Int) {
        viewModelScope.launch {
            val workoutWithEx = repository.getWorkoutById(workoutId).first()
            if (workoutWithEx != null) {
                val updatedExercises = workoutWithEx.exercises.map { exWithSets ->
                    if (exWithSets.exercise.id == exerciseId) {
                        val updatedSets = exWithSets.sets.map { set ->
                            if (set.id == setId) {
                                set.copy(targetWeight = weight, targetReps = reps, restInterval = rest)
                            } else {
                                set
                            }
                        }
                        exWithSets.copy(sets = updatedSets)
                    } else {
                        exWithSets
                    }
                }
                repository.addWorkout(workoutWithEx.workout, updatedExercises)
            }
        }
    }

    fun addSet(workoutId: Long, exerciseId: Long) {
        viewModelScope.launch {
            val workoutWithEx = repository.getWorkoutById(workoutId).first()
            if (workoutWithEx != null) {
                val updatedExercises = workoutWithEx.exercises.map { exWithSets ->
                    if (exWithSets.exercise.id == exerciseId) {
                        val newSet = SetEntity(
                            exerciseId = exerciseId,
                            targetWeight = 0.0,
                            targetReps = 0,
                            order = exWithSets.sets.size
                        )
                        exWithSets.copy(sets = exWithSets.sets + newSet)
                    } else {
                        exWithSets
                    }
                }
                repository.addWorkout(workoutWithEx.workout, updatedExercises)
            }
        }
    }
}

sealed interface WorkoutUiState {
    data object Idle : WorkoutUiState
    data object Loading : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
}
