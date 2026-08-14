package digital.tonima.myworkout.wear.ui

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.myworkout.BuildConfig
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import digital.tonima.myworkout.wear.WorkoutService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val alertManager: AlertManager,
        @ApplicationContext private val context: Context,
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
                                            restInterval = 20,
                                            order = 0,
                                            exerciseId = 0,
                                        ),
                                        SetEntity(
                                            targetWeight = 60.0,
                                            targetReps = 10,
                                            restInterval = 20,
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
                                            restInterval = 20,
                                            order = 0,
                                            exerciseId = 0,
                                        ),
                                        SetEntity(
                                            targetWeight = 80.0,
                                            targetReps = 8,
                                            restInterval = 20,
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
            if (_activeSession.value?.session?.workoutId == workoutId) return

            viewModelScope.launch {
                val sessionId = repository.startSession(workoutId)
                repository.getSessionById(sessionId).collect { session ->
                    _activeSession.value = session

                    val workout = _currentWorkout.value
                    if (session != null && workout != null) {
                        val intent =
                            Intent(context, WorkoutService::class.java).apply {
                                putExtra("workout_name", workout.workout.name)
                                putExtra("workout_id", workout.workout.id)
                            }
                        context.startForegroundService(intent)
                    }
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
                if (restInterval > 0) {
                    val endTime = SystemClock.elapsedRealtime() + (restInterval * 1000L)
                    val intent =
                        Intent(context, WorkoutService::class.java).apply {
                            action = WorkoutService.ACTION_UPDATE_TIMER
                            putExtra("rest_end_time", endTime)
                        }
                    context.startService(intent)

                    startRestTimer(restInterval)
                }
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

                val resetIntent =
                    Intent(context, WorkoutService::class.java).apply {
                        action = WorkoutService.ACTION_UPDATE_TIMER
                        putExtra("rest_end_time", 0L)
                    }
                context.startService(resetIntent)

                alertManager.triggerCompletionAlert()
            }
        }

        fun finishSession() {
            val session = _activeSession.value ?: return
            viewModelScope.launch {
                repository.finishSession(session.session)
                _activeSession.value = null
                _currentWorkout.value = null

                val intent =
                    Intent(context, WorkoutService::class.java).apply {
                        action = WorkoutService.ACTION_STOP
                    }
                context.startService(intent)
            }
        }
    }
