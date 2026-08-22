package digital.tonima.myworkout.data.util

import digital.tonima.myworkout.data.model.WorkoutWithExercises
import kotlinx.serialization.json.Json

object WorkoutSharingUtils {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    fun WorkoutWithExercises.toShareableText(): String {
        val sb = StringBuilder()
        sb.append("💪 Workout: ${workout.name}\n")
        if (workout.description.isNotEmpty()) {
            sb.append("${workout.description}\n")
        }
        sb.append("\n")
        exercises.forEach { ex ->
            sb.append("🏋️ ${ex.exercise.name}\n")
            ex.sets.forEachIndexed { index, set ->
                sb.append("  ${index + 1}. ${set.targetWeight}kg x ${set.targetReps}")
                if (set.restInterval > 0) {
                    sb.append(" (Rest: ${set.restInterval}s)")
                }
                sb.append("\n")
            }
            sb.append("\n")
        }
        return sb.toString().trim()
    }

    fun WorkoutWithExercises.toJson(): String {
        return json.encodeToString(this)
    }

    fun decodeJsonToWorkout(jsonString: String): WorkoutWithExercises? {
        return try {
            json.decodeFromString<WorkoutWithExercises>(jsonString)
        } catch (_: Exception) {
            null
        }
    }

    fun WorkoutWithExercises.validate(): Boolean {
        if (workout.name.isBlank() || workout.name.length > 100) return false
        if (exercises.isEmpty() || exercises.size > 50) return false

        return exercises.all { ex ->
            ex.exercise.name.isNotBlank() &&
                ex.exercise.name.length <= 100 &&
                ex.sets.isNotEmpty() &&
                ex.sets.size <= 50 &&
                ex.sets.all { set ->
                    set.targetWeight in 0.0..1000.0 &&
                        set.targetReps in 0..1000 &&
                        set.restInterval in 0..3600
                }
        }
    }
}
