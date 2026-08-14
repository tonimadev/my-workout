package digital.tonima.myworkout.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.BuildConfig
import digital.tonima.myworkout.data.model.*
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val alertManager: AlertManager,
    ) : ViewModel() {
        init {
            viewModelScope.launch {
                repository.requestSync()
            }
            if (BuildConfig.DEBUG) {
                injectDebugData()
            }
        }

        private fun injectDebugData() {
            viewModelScope.launch {
                if (repository.getAllWorkouts().first().isEmpty()) {
                    val benchId = repository.addMasterExercise("Bench Press")
                    val squatId = repository.addMasterExercise("Squat")

                    val workout = WorkoutEntity(name = "Debug Workout")
                    val exercises =
                        listOf(
                            ExerciseWithSets(
                                exercise =
                                    ExerciseEntity(
                                        name = "Bench Press",
                                        masterExerciseId = benchId,
                                        order = 0,
                                        workoutId = 0,
                                    ),
                                sets =
                                    listOf(
                                        SetEntity(
                                            targetWeight = 60.0,
                                            targetReps = 10,
                                            restInterval = 60,
                                            order = 0,
                                            exerciseId = 0,
                                        ),
                                        SetEntity(
                                            targetWeight = 60.0,
                                            targetReps = 10,
                                            restInterval = 60,
                                            order = 1,
                                            exerciseId = 0,
                                        ),
                                    ),
                            ),
                            ExerciseWithSets(
                                exercise =
                                    ExerciseEntity(
                                        name = "Squat",
                                        masterExerciseId = squatId,
                                        order = 1,
                                        workoutId = 0,
                                    ),
                                sets =
                                    listOf(
                                        SetEntity(
                                            targetWeight = 80.0,
                                            targetReps = 8,
                                            restInterval = 90,
                                            order = 0,
                                            exerciseId = 0,
                                        ),
                                        SetEntity(
                                            targetWeight = 80.0,
                                            targetReps = 8,
                                            restInterval = 90,
                                            order = 1,
                                            exerciseId = 0,
                                        ),
                                    ),
                            ),
                        )
                    repository.addWorkout(workout, exercises)
                }
            }
        }

        val workouts =
            repository.getAllWorkouts()
                .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

        private val _activeSession = MutableStateFlow<SessionWithLogs?>(null)
        val activeSession: StateFlow<SessionWithLogs?> = _activeSession.asStateFlow()

        private val _currentWorkout = MutableStateFlow<WorkoutWithExercises?>(null)
        val currentWorkout: StateFlow<WorkoutWithExercises?> = _currentWorkout.asStateFlow()

        private val _restTimeRemaining = MutableStateFlow(0L)
        val restTimeRemaining: StateFlow<Long> = _restTimeRemaining.asStateFlow()

        private val _totalRestTime = MutableStateFlow(0L)
        val totalRestTime: StateFlow<Long> = _totalRestTime.asStateFlow()

        private val _isResting = MutableStateFlow(false)
        val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

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
                val masterExerciseId =
                    _currentWorkout.value?.exercises
                        ?.find { it.exercise.id == exerciseId }?.exercise?.masterExerciseId ?: 0L

                val log =
                    WorkoutLogEntity(
                        sessionId = session.session.id,
                        masterExerciseId = masterExerciseId,
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
                _totalRestTime.value = seconds.toLong()
                _restTimeRemaining.value = seconds.toLong()
                _isResting.value = true
                while (_restTimeRemaining.value > 0) {
                    delay(1000.milliseconds)
                    _restTimeRemaining.value -= 1
                }
                // Wait for the last second of animation to complete on screen
                delay(1100.milliseconds)
                _isResting.value = false
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
