package digital.tonima.myworkout.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

        private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
        val uiState: StateFlow<WorkoutUiState> = _uiState

        fun deleteWorkout(workout: WorkoutEntity) {
            viewModelScope.launch {
                repository.deleteWorkout(workout)
            }
        }

        fun syncWorkouts() {
            viewModelScope.launch {
                // repository.addWorkout calls sync internally, but we can force it by re-saving
                val all = repository.getAllWorkouts().first()
                all.forEach {
                    repository.addWorkout(it.workout, it.exercises)
                }
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

        private val _restTimeRemaining = MutableStateFlow(0)
        val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()

        private val _totalRestTime = MutableStateFlow(0)
        val totalRestTime: StateFlow<Int> = _totalRestTime.asStateFlow()

        private var restJob: Job? = null

        fun startWorkout(workoutId: Long) {
            viewModelScope.launch {
                val sessionId = repository.startSession(workoutId)
                repository.getSessionById(sessionId).collect { session ->
                    _activeSession.value = session
                }
            }
        }

        fun logSet(
            sessionId: Long,
            exerciseId: Long,
            setId: Long,
            weight: Double,
            reps: Int,
            restInterval: Int,
        ) {
            viewModelScope.launch {
                val workoutId = _activeSession.value?.session?.workoutId
                val masterExerciseId =
                    if (workoutId != null) {
                        repository.getWorkoutById(workoutId).first()?.exercises
                            ?.find { it.exercise.id == exerciseId }?.exercise?.masterExerciseId ?: 0L
                    } else {
                        0L
                    }

                repository.addLog(
                    WorkoutLogEntity(
                        sessionId = sessionId,
                        masterExerciseId = masterExerciseId,
                        setId = setId,
                        actualWeight = weight,
                        actualReps = reps,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
                if (restInterval > 0) {
                    startRestTimer(restInterval)
                }
            }
        }

        private fun startRestTimer(seconds: Int) {
            restJob?.cancel()
            restJob =
                viewModelScope.launch {
                    _totalRestTime.value = seconds
                    _restTimeRemaining.value = seconds
                    while (_restTimeRemaining.value > 0) {
                        delay(1000)
                        _restTimeRemaining.value -= 1
                    }
                    alertManager.triggerCompletionAlert()
                }
        }

        fun skipRest() {
            restJob?.cancel()
            _restTimeRemaining.value = 0
            _totalRestTime.value = 0
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

        fun addExercise(
            workoutId: Long,
            name: String,
        ) {
            viewModelScope.launch {
                val masterExercises = repository.getAllMasterExercises().first()
                val masterId =
                    masterExercises.find { it.name.equals(name, ignoreCase = true) }?.id
                        ?: repository.addMasterExercise(name)

                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val newExercise =
                        ExerciseEntity(
                            workoutId = workoutId,
                            masterExerciseId = masterId,
                            name = name,
                            order = workoutWithEx.exercises.size,
                        )
                    repository.addWorkout(
                        workoutWithEx.workout,
                        workoutWithEx.exercises + ExerciseWithSets(newExercise, emptyList()),
                    )
                }
            }
        }

        fun updateSet(
            workoutId: Long,
            exerciseId: Long,
            setId: Long,
            weight: Double,
            reps: Int,
            rest: Int,
        ) {
            viewModelScope.launch {
                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val updatedExercises =
                        workoutWithEx.exercises.map { exWithSets ->
                            if (exWithSets.exercise.id == exerciseId) {
                                val updatedSets =
                                    exWithSets.sets.map { set ->
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

        fun addSet(
            workoutId: Long,
            exerciseId: Long,
        ) {
            viewModelScope.launch {
                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val updatedExercises =
                        workoutWithEx.exercises.map { exWithSets ->
                            if (exWithSets.exercise.id == exerciseId) {
                                val lastSet = exWithSets.sets.maxByOrNull { it.order }
                                val newSet =
                                    SetEntity(
                                        exerciseId = exerciseId,
                                        targetWeight = lastSet?.targetWeight ?: 0.0,
                                        targetReps = lastSet?.targetReps ?: 0,
                                        restInterval = lastSet?.restInterval ?: 60,
                                        order = exWithSets.sets.size,
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

        fun deleteSet(
            workoutId: Long,
            exerciseId: Long,
            setId: Long,
        ) {
            viewModelScope.launch {
                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val updatedExercises =
                        workoutWithEx.exercises.map { exWithSets ->
                            if (exWithSets.exercise.id == exerciseId) {
                                val updatedSets =
                                    exWithSets.sets.filterNot { it.id == setId }
                                        .mapIndexed { index, set -> set.copy(order = index) }
                                exWithSets.copy(sets = updatedSets)
                            } else {
                                exWithSets
                            }
                        }
                    repository.addWorkout(workoutWithEx.workout, updatedExercises)
                }
            }
        }

        fun duplicateExercise(
            workoutId: Long,
            exerciseId: Long,
        ) {
            viewModelScope.launch {
                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val exerciseToDuplicate = workoutWithEx.exercises.find { it.exercise.id == exerciseId }
                    if (exerciseToDuplicate != null) {
                        val newExercise =
                            exerciseToDuplicate.exercise.copy(
                                id = 0,
                                order = workoutWithEx.exercises.size,
                            )
                        val newSets = exerciseToDuplicate.sets.map { it.copy(id = 0) }
                        val updatedExercises = workoutWithEx.exercises + ExerciseWithSets(newExercise, newSets)
                        repository.addWorkout(workoutWithEx.workout, updatedExercises)
                    }
                }
            }
        }

        fun deleteExercise(
            workoutId: Long,
            exerciseId: Long,
        ) {
            viewModelScope.launch {
                val workoutWithEx = repository.getWorkoutById(workoutId).first()
                if (workoutWithEx != null) {
                    val updatedExercises =
                        workoutWithEx.exercises.filterNot { it.exercise.id == exerciseId }
                            .mapIndexed { index, exWithSets ->
                                exWithSets.copy(exercise = exWithSets.exercise.copy(order = index))
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
