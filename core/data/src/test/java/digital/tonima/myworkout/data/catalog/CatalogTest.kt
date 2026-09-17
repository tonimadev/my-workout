package digital.tonima.myworkout.data.catalog

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [WorkoutTemplateCatalog] resolves its exercises by looking up [ExerciseCatalog.all] by name at
 * object-initialization time (see `WorkoutTemplateCatalog.exercise`), which throws and crashes
 * the app the first time either catalog is touched (e.g. onboarding) if a name ever drifts out of
 * sync between the two lists. These tests exist to catch that drift in CI instead of at runtime.
 */
class CatalogTest {
    @Test
    fun `every catalog exercise name is unique`() {
        val names = ExerciseCatalog.all.map { it.name }

        assertTrue(
            "Duplicate exercise names found: ${names.groupBy { it }.filterValues { it.size > 1 }.keys}",
            names.size == names.toSet().size,
        )
    }

    @Test
    fun `every catalog exercise has at least one default set`() {
        ExerciseCatalog.all.forEach { exercise ->
            assertTrue("${exercise.name} has no default sets", exercise.defaultSets.isNotEmpty())
        }
    }

    @Test
    fun `every template resolves all of its exercises from the catalog`() {
        // WorkoutTemplateCatalog.all is a lazily-safe check here: any lookup failure inside
        // `exercise(name)` throws NoSuchElementException while building this list.
        assertTrue(WorkoutTemplateCatalog.all.isNotEmpty())
        WorkoutTemplateCatalog.all.forEach { template ->
            assertTrue("${template.name} has no exercises", template.exercises.isNotEmpty())
        }
    }

    @Test
    fun `every template exercise is a reference into ExerciseCatalog, not a stray copy`() {
        val catalogNames = ExerciseCatalog.all.map { it.name }.toSet()

        WorkoutTemplateCatalog.all.forEach { template ->
            template.exercises.forEach { exercise ->
                assertTrue(
                    "${exercise.name} in ${template.name} is not in ExerciseCatalog",
                    exercise.name in catalogNames,
                )
            }
        }
    }
}
