package digital.tonima.myworkout.features.workout.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.model.WorkoutWithExercises
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.AlertManager
import digital.tonima.myworkout.data.util.WorkoutSharingUtils
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toJson
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toShareableText
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.validate
import digital.tonima.myworkout.ui.util.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class WorkoutState(
    val workouts: List<WorkoutWithExercises> = emptyList(),
    val selectedWorkout: WorkoutWithExercises? = null,
    val activeSession: SessionWithLogs? = null,
    val restTimeRemaining: Int = 0,
    val totalRestTime: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shareText: String? = null,
    val exportJson: String? = null,
    val shouldNavigateBack: Boolean = false,
)

sealed interface WorkoutIntent {
    data object LoadWorkouts : WorkoutIntent

    data class LoadWorkout(val id: Long) : WorkoutIntent

    data class AddWorkout(val name: String) : WorkoutIntent

    data class DeleteWorkout(val workout: WorkoutEntity) : WorkoutIntent

    data object SyncWorkouts : WorkoutIntent

    data class StartWorkout(val workoutId: Long) : WorkoutIntent

    data class LogSet(
        val sessionId: Long,
        val exerciseId: Long,
        val setId: Long,
        val weight: Double,
        val reps: Int,
        val restInterval: Int,
    ) : WorkoutIntent

    data object SkipRest : WorkoutIntent

    data object FinishWorkout : WorkoutIntent

    data class AddExercise(
        val workoutId: Long,
        val name: String,
    ) : WorkoutIntent

    data class AddSet(
        val workoutId: Long,
        val exerciseId: Long,
    ) : WorkoutIntent

    data class UpdateSet(
        val workoutId: Long,
        val exerciseId: Long,
        val setId: Long,
        val weight: Double,
        val reps: Int,
        val rest: Int,
    ) : WorkoutIntent

    data class DeleteSet(
        val workoutId: Long,
        val exerciseId: Long,
        val setId: Long,
    ) : WorkoutIntent

    data class DuplicateExercise(
        val workoutId: Long,
        val exerciseId: Long,
    ) : WorkoutIntent

    data class DeleteExercise(
        val workoutId: Long,
        val exerciseId: Long,
    ) : WorkoutIntent

    data class ShareWorkout(val workout: WorkoutWithExercises) : WorkoutIntent

    data class ExportWorkout(val workout: WorkoutWithExercises) : WorkoutIntent

    data class ImportWorkout(val json: String) : WorkoutIntent

    data object ClearShareData : WorkoutIntent

    data object ClearError : WorkoutIntent

    data object ResetNavigation : WorkoutIntent
}

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val alertManager: AlertManager,
    ) : MviViewModel<WorkoutState, WorkoutIntent>(WorkoutState()) {
        private var restJob: Job? = null

        init {
            observeWorkouts()
        }

        private fun observeWorkouts() {
            viewModelScope.launch {
                repository.getAllWorkouts().collect { workouts ->
                    updateState { copy(workouts = workouts) }
                }
            }
        }

        private fun observeWorkout(id: Long) {
            viewModelScope.launch {
                repository.getWorkoutById(id).collect { workout ->
                    updateState { copy(selectedWorkout = workout) }
                }
            }
        }

        override fun handleIntent(intent: WorkoutIntent) {
            when (intent) {
                is WorkoutIntent.LoadWorkouts -> observeWorkouts()
                is WorkoutIntent.LoadWorkout -> observeWorkout(intent.id)
                is WorkoutIntent.AddWorkout -> addWorkout(intent.name)
                is WorkoutIntent.DeleteWorkout -> deleteWorkout(intent.workout)
                is WorkoutIntent.SyncWorkouts -> syncWorkouts()
                is WorkoutIntent.StartWorkout -> startWorkout(intent.workoutId)
                is WorkoutIntent.LogSet ->
                    logSet(
                        intent.sessionId,
                        intent.exerciseId,
                        intent.setId,
                        intent.weight,
                        intent.reps,
                        intent.restInterval,
                    )
                is WorkoutIntent.SkipRest -> skipRest()
                is WorkoutIntent.FinishWorkout -> finishWorkout()
                is WorkoutIntent.AddExercise -> addExercise(intent.workoutId, intent.name)
                is WorkoutIntent.AddSet -> addSet(intent.workoutId, intent.exerciseId)
                is WorkoutIntent.UpdateSet ->
                    updateSet(
                        intent.workoutId,
                        intent.exerciseId,
                        intent.setId,
                        intent.weight,
                        intent.reps,
                        intent.rest,
                    )
                is WorkoutIntent.DeleteSet -> deleteSet(intent.workoutId, intent.exerciseId, intent.setId)
                is WorkoutIntent.DuplicateExercise -> duplicateExercise(intent.workoutId, intent.exerciseId)
                is WorkoutIntent.DeleteExercise -> deleteExercise(intent.workoutId, intent.exerciseId)
                is WorkoutIntent.ShareWorkout -> shareWorkout(intent.workout)
                is WorkoutIntent.ExportWorkout -> exportWorkout(intent.workout)
                is WorkoutIntent.ImportWorkout -> importWorkout(intent.json)
                is WorkoutIntent.ClearShareData -> updateState { copy(shareText = null, exportJson = null) }
                is WorkoutIntent.ClearError -> updateState { copy(error = null) }
                is WorkoutIntent.ResetNavigation -> updateState { copy(shouldNavigateBack = false) }
            }
        }

        private fun deleteWorkout(workout: WorkoutEntity) {
            viewModelScope.launch {
                repository.deleteWorkout(workout)
            }
        }

        private fun syncWorkouts() {
            viewModelScope.launch {
                val all = repository.getAllWorkouts().first()
                all.forEach {
                    repository.addWorkout(it.workout, it.exercises)
                }
            }
        }

        private fun addWorkout(name: String) {
            viewModelScope.launch {
                repository.addWorkout(WorkoutEntity(name = name), emptyList())
            }
        }

        private fun startWorkout(workoutId: Long) {
            viewModelScope.launch {
                val sessionId = repository.startSession(workoutId)
                repository.getSessionById(sessionId).collect { session ->
                    updateState { copy(activeSession = session) }
                }
            }
        }

        private fun logSet(
            sessionId: Long,
            exerciseId: Long,
            setId: Long,
            weight: Double,
            reps: Int,
            restInterval: Int,
        ) {
            viewModelScope.launch {
                val workoutId = currentState.activeSession?.session?.workoutId
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
                    updateState { copy(totalRestTime = seconds, restTimeRemaining = seconds) }
                    while (currentState.restTimeRemaining > 0) {
                        delay(1000.milliseconds)
                        updateState { copy(restTimeRemaining = restTimeRemaining - 1) }
                    }
                    alertManager.triggerCompletionAlert()
                }
        }

        private fun skipRest() {
            restJob?.cancel()
            updateState { copy(restTimeRemaining = 0, totalRestTime = 0) }
        }

        private fun finishWorkout() {
            viewModelScope.launch {
                val session = currentState.activeSession?.session
                if (session != null) {
                    repository.finishSession(session)
                    updateState { copy(activeSession = null, shouldNavigateBack = true) }
                }
            }
        }

        fun getWorkoutFlow(id: Long) = repository.getWorkoutById(id)

        private fun addExercise(
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

        private fun updateSet(
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

        private fun addSet(
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

        private fun deleteSet(
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

        private fun duplicateExercise(
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

        private fun deleteExercise(
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

        private fun shareWorkout(workout: WorkoutWithExercises) {
            updateState { copy(shareText = workout.toShareableText()) }
        }

        private fun exportWorkout(workout: WorkoutWithExercises) {
            updateState { copy(exportJson = workout.toJson()) }
        }

        private fun importWorkout(json: String) {
            viewModelScope.launch {
                val workout = WorkoutSharingUtils.decodeJsonToWorkout(json)
                if (workout != null) {
                    if (WorkoutSharingUtils.run { workout.validate() }) {
                        repository.importWorkout(workout)
                    } else {
                        updateState { copy(error = "import_error_validation") }
                    }
                } else {
                    updateState { copy(error = "import_error_format") }
                }
            }
        }
    }
