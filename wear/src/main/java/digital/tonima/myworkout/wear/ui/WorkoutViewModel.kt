package digital.tonima.myworkout.wear.ui

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.Immutable
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
import digital.tonima.myworkout.wear.ui.util.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class WorkoutState(
    val workouts: List<WorkoutWithExercises> = emptyList(),
    val activeSession: SessionWithLogs? = null,
    val currentWorkout: WorkoutWithExercises? = null,
    val restTimeRemaining: Long = 0,
    val totalRestTime: Long = 0,
    val isResting: Boolean = false,
    val lastXpGained: Int? = null,
    val shouldNavigateBack: Boolean = false,
)

sealed interface WorkoutIntent {
    data object RequestSync : WorkoutIntent

    data class LoadWorkout(val workoutId: Long) : WorkoutIntent

    data class StartSession(val workoutId: Long) : WorkoutIntent

    data class CompleteSet(
        val exerciseId: Long,
        val setId: Long,
        val weight: Float,
        val reps: Int,
        val restInterval: Int,
    ) : WorkoutIntent

    data object SkipRest : WorkoutIntent

    data object FinishSession : WorkoutIntent

    data object ResetNavigation : WorkoutIntent
}

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val alertManager: AlertManager,
        @ApplicationContext private val context: Context,
    ) : MviViewModel<WorkoutState, WorkoutIntent>(WorkoutState()) {
        private var restJob: Job? = null

        init {
            onIntent(WorkoutIntent.RequestSync)
            observeWorkouts()
            if (BuildConfig.DEBUG) {
                injectDebugData()
            }
        }

        private fun observeWorkouts() {
            viewModelScope.launch {
                repository.getAllWorkouts().collect { workouts ->
                    updateState { copy(workouts = workouts) }
                }
            }
        }

        override fun handleIntent(intent: WorkoutIntent) {
            when (intent) {
                is WorkoutIntent.RequestSync -> viewModelScope.launch { repository.requestSync() }
                is WorkoutIntent.LoadWorkout -> loadWorkout(intent.workoutId)
                is WorkoutIntent.StartSession -> startSession(intent.workoutId)
                is WorkoutIntent.CompleteSet ->
                    completeSet(
                        intent.exerciseId,
                        intent.setId,
                        intent.weight,
                        intent.reps,
                        intent.restInterval,
                    )
                is WorkoutIntent.SkipRest -> skipRest()
                is WorkoutIntent.FinishSession -> finishSession()
                is WorkoutIntent.ResetNavigation -> updateState { copy(shouldNavigateBack = false) }
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
                    repository.addWorkout(
                        workout.copy(
                            name = "Debug Workout 2",
                        ),
                        exercises,
                    )
                    repository.addWorkout(
                        workout.copy(
                            name = "Debug Workout 3",
                        ),
                        exercises,
                    )
                    repository.addWorkout(
                        workout.copy(
                            name = "Debug Workout 4",
                        ),
                        exercises,
                    )
                    repository.addWorkout(
                        workout.copy(
                            name = "Debug Workout 5",
                        ),
                        exercises,
                    )
                }
            }
        }

        private fun loadWorkout(workoutId: Long) {
            viewModelScope.launch {
                repository.getWorkoutById(workoutId).collect { workout ->
                    updateState { copy(currentWorkout = workout) }
                }
            }
        }

        private fun startSession(workoutId: Long) {
            if (currentState.activeSession?.session?.workoutId == workoutId) return

            viewModelScope.launch {
                val sessionId = repository.startSession(workoutId)
                repository.getSessionById(sessionId).collect { session ->
                    updateState { copy(activeSession = session) }

                    val workout = currentState.currentWorkout
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

        private fun completeSet(
            exerciseId: Long,
            setId: Long,
            weight: Float,
            reps: Int,
            restInterval: Int,
        ) {
            val session = currentState.activeSession ?: return
            viewModelScope.launch {
                val masterExerciseId =
                    currentState.currentWorkout?.exercises
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

                // Show XP feedback
                updateState { copy(lastXpGained = 10) }
                viewModelScope.launch {
                    delay(2000.milliseconds)
                    if (currentState.lastXpGained == 10) {
                        updateState { copy(lastXpGained = null) }
                    }
                }

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
            restJob?.cancel()
            restJob =
                viewModelScope.launch {
                    updateState {
                        copy(
                            totalRestTime = seconds.toLong(),
                            restTimeRemaining = seconds.toLong(),
                            isResting = true,
                        )
                    }
                    while (currentState.restTimeRemaining > 0) {
                        delay(1000.milliseconds)
                        updateState { copy(restTimeRemaining = restTimeRemaining - 1) }
                    }
                    // Wait for the last second of animation to complete on screen
                    delay(1100.milliseconds)
                    updateState { copy(isResting = false) }

                    val resetIntent =
                        Intent(context, WorkoutService::class.java).apply {
                            action = WorkoutService.ACTION_UPDATE_TIMER
                            putExtra("rest_end_time", 0L)
                        }
                    context.startService(resetIntent)

                    alertManager.triggerCompletionAlert()
                }
        }

        private fun skipRest() {
            restJob?.cancel()
            updateState { copy(restTimeRemaining = 0, isResting = false) }

            val resetIntent =
                Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_UPDATE_TIMER
                    putExtra("rest_end_time", 0L)
                }
            context.startService(resetIntent)
        }

        private fun finishSession() {
            val session = currentState.activeSession ?: return
            viewModelScope.launch {
                repository.finishSession(session.session)
                updateState { copy(activeSession = null, currentWorkout = null, shouldNavigateBack = true) }

                val intent =
                    Intent(context, WorkoutService::class.java).apply {
                        action = WorkoutService.ACTION_STOP
                    }
                context.startService(intent)
            }
        }
    }
