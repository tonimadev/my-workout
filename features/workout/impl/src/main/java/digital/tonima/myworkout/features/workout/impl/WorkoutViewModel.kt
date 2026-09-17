package digital.tonima.myworkout.features.workout.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.tonima.myworkout.data.catalog.WorkoutTemplate
import digital.tonima.myworkout.data.model.ExerciseEntity
import digital.tonima.myworkout.data.model.ExerciseWithSets
import digital.tonima.myworkout.data.model.MuscleGroup
import digital.tonima.myworkout.data.model.SetEntity
import digital.tonima.myworkout.data.model.WorkoutEntity
import digital.tonima.myworkout.data.model.WorkoutLogEntity
import digital.tonima.myworkout.data.repository.WorkoutRepository
import digital.tonima.myworkout.data.util.RestTimerController
import digital.tonima.myworkout.data.util.WorkoutSharingUtils
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toJson
import digital.tonima.myworkout.data.util.WorkoutSharingUtils.toShareableText
import digital.tonima.myworkout.ui.model.SessionUiModel
import digital.tonima.myworkout.ui.model.WorkoutUiModel
import digital.tonima.myworkout.ui.util.MviViewModel
import digital.tonima.myworkout.ui.util.toUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class WorkoutState(
    val workouts: ImmutableList<WorkoutUiModel> = persistentListOf(),
    val selectedWorkout: WorkoutUiModel? = null,
    val activeSession: SessionUiModel? = null,
    val restTimeRemaining: Int = 0,
    val totalRestTime: Int = 0,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val shareText: String? = null,
    val exportJson: String? = null,
    val syncMessage: String? = null,
    val shouldNavigateBack: Boolean = false,
    val newlyCreatedWorkoutId: Long? = null,
)

sealed interface WorkoutIntent {
    data object LoadWorkouts : WorkoutIntent

    data class LoadWorkout(val id: Long) : WorkoutIntent

    data class AddWorkout(val name: String) : WorkoutIntent

    data class DeleteWorkout(val id: Long) : WorkoutIntent

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
        val primaryMuscle: MuscleGroup? = null,
        val secondaryMuscles: List<MuscleGroup> = emptyList(),
    ) : WorkoutIntent

    data class ApplyTemplate(val template: WorkoutTemplate) : WorkoutIntent

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

    data class ShareWorkout(val workout: WorkoutUiModel) : WorkoutIntent

    data class ExportWorkout(val workout: WorkoutUiModel) : WorkoutIntent

    data class ImportWorkout(val json: String) : WorkoutIntent

    data object ClearShareData : WorkoutIntent

    data object ClearNewWorkoutId : WorkoutIntent

    data object ClearError : WorkoutIntent

    data object ClearSyncMessage : WorkoutIntent

    data object ResetNavigation : WorkoutIntent
}

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val repository: WorkoutRepository,
        private val restTimerController: RestTimerController,
    ) : MviViewModel<WorkoutState, WorkoutIntent>(WorkoutState()) {
        init {
            observeWorkouts()
            observeRestTimer()
        }

        // The countdown itself lives in RestTimerController (backed by a foreground service on
        // Android) so rest alerts still fire on time if the app is backgrounded or the screen is
        // off mid-rest; this just mirrors that state into the UI.
        private fun observeRestTimer() {
            viewModelScope.launch {
                restTimerController.restTimeRemaining.collect { remaining ->
                    updateState { copy(restTimeRemaining = remaining) }
                }
            }
            viewModelScope.launch {
                restTimerController.totalRestTime.collect { total ->
                    updateState { copy(totalRestTime = total) }
                }
            }
        }

        private fun observeWorkouts() {
            viewModelScope.launch {
                repository.getAllWorkouts().collect { workouts ->
                    updateState { copy(workouts = workouts.map { it.toUiModel() }.toImmutableList()) }
                }
            }
        }

        private fun observeWorkout(id: Long) {
            viewModelScope.launch {
                repository.getWorkoutById(id).collect { workout ->
                    updateState { copy(selectedWorkout = workout?.toUiModel()) }
                }
            }
        }

        override fun handleIntent(intent: WorkoutIntent) {
            when (intent) {
                is WorkoutIntent.LoadWorkouts -> observeWorkouts()
                is WorkoutIntent.LoadWorkout -> observeWorkout(intent.id)
                is WorkoutIntent.AddWorkout -> addWorkout(intent.name)
                is WorkoutIntent.DeleteWorkout -> deleteWorkout(intent.id)
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
                is WorkoutIntent.AddExercise ->
                    addExercise(intent.workoutId, intent.name, intent.primaryMuscle, intent.secondaryMuscles)
                is WorkoutIntent.ApplyTemplate -> applyTemplate(intent.template)
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
                is WorkoutIntent.ClearNewWorkoutId -> updateState { copy(newlyCreatedWorkoutId = null) }
                is WorkoutIntent.ClearError -> updateState { copy(error = null) }
                is WorkoutIntent.ClearSyncMessage -> updateState { copy(syncMessage = null) }
                is WorkoutIntent.ResetNavigation -> updateState { copy(shouldNavigateBack = false) }
            }
        }

        private fun deleteWorkout(id: Long) {
            viewModelScope.launch {
                val workout = repository.getWorkoutById(id).first()
                if (workout != null) {
                    repository.deleteWorkout(workout.workout)
                }
            }
        }

        private fun syncWorkouts() {
            viewModelScope.launch {
                updateState { copy(isSyncing = true) }
                repository.forceSync()
                updateState { copy(isSyncing = false, syncMessage = "sync_success") }
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
                    updateState { copy(activeSession = session?.toUiModel()) }
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
                val workoutId = currentState.activeSession?.workoutId
                val workoutWithEx =
                    if (workoutId != null) {
                        repository.getWorkoutById(workoutId).first()
                    } else {
                        null
                    }

                val masterExerciseId =
                    workoutWithEx?.exercises?.find { it.exercise.id == exerciseId }?.exercise?.masterExerciseId ?: 0L

                // Auto-update workout template if weight or reps changed
                if (workoutId != null && workoutWithEx != null) {
                    val currentSet =
                        workoutWithEx.exercises
                            .flatMap { it.sets }
                            .find { it.id == setId }

                    if (currentSet != null && (currentSet.targetWeight != weight || currentSet.targetReps != reps)) {
                        updateWorkoutTemplate(workoutId, exerciseId, setId, weight, reps, restInterval)
                    }
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
                    restTimerController.startRest(restInterval)
                }
            }
        }

        private fun skipRest() {
            restTimerController.stop()
        }

        private fun finishWorkout() {
            viewModelScope.launch {
                val sessionId = currentState.activeSession?.id
                if (sessionId != null) {
                    repository.finishSession(sessionId)
                    restTimerController.stop()
                    updateState { copy(activeSession = null, shouldNavigateBack = true) }
                }
            }
        }

        fun getWorkoutFlow(id: Long) = repository.getWorkoutById(id)

        private fun addExercise(
            workoutId: Long,
            name: String,
            primaryMuscle: MuscleGroup?,
            secondaryMuscles: List<MuscleGroup>,
        ) {
            viewModelScope.launch {
                val masterId = repository.resolveOrCreateMasterExercise(name, primaryMuscle, secondaryMuscles)

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

        private fun applyTemplate(template: WorkoutTemplate) {
            viewModelScope.launch {
                val workoutId = repository.applyWorkoutTemplate(template)
                updateState { copy(newlyCreatedWorkoutId = workoutId) }
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
                updateWorkoutTemplate(workoutId, exerciseId, setId, weight, reps, rest)
            }
        }

        private suspend fun updateWorkoutTemplate(
            workoutId: Long,
            exerciseId: Long,
            setId: Long,
            weight: Double,
            reps: Int,
            rest: Int,
        ) {
            val workoutWithEx = repository.getWorkoutById(workoutId).first()
            if (workoutWithEx != null) {
                val updatedExercises =
                    workoutWithEx.exercises.map { exWithSets ->
                        if (exWithSets.exercise.id == exerciseId) {
                            var foundTarget = false
                            val updatedSets =
                                // Sets aren't guaranteed to come back in display order (Room's
                                // @Relation query has no ORDER BY), so "subsequent" must be
                                // determined by the `order` field, not by list position.
                                exWithSets.sets.sortedBy { it.order }.map { set ->
                                    if (set.id == setId) {
                                        foundTarget = true
                                        set.copy(targetWeight = weight, targetReps = reps, restInterval = rest)
                                    } else if (foundTarget) {
                                        // Propagate weight and reps to subsequent sets of the same exercise
                                        set.copy(targetWeight = weight, targetReps = reps)
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
                                        .sortedBy { it.order }
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

        private fun shareWorkout(workout: WorkoutUiModel) {
            val workoutId = workout.id
            viewModelScope.launch {
                val domainWorkout = repository.getWorkoutById(workoutId).first()
                if (domainWorkout != null) {
                    updateState { copy(shareText = domainWorkout.toShareableText()) }
                }
            }
        }

        private fun exportWorkout(workout: WorkoutUiModel) {
            val workoutId = workout.id
            viewModelScope.launch {
                val domainWorkout = repository.getWorkoutById(workoutId).first()
                if (domainWorkout != null) {
                    updateState { copy(exportJson = domainWorkout.toJson()) }
                }
            }
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
