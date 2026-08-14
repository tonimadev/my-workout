package digital.tonima.myworkout.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setup() {
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = { temporaryFolder.newFile("test.preferences_pb") },
            )
        repository = UserPreferencesRepository(dataStore)
    }

    @Test
    fun onboardingCompleted_defaultsToFalse() =
        runTest(testDispatcher) {
            repository.onboardingCompleted.test {
                assertEquals(false, awaitItem())
            }
        }

    @Test
    fun setOnboardingCompleted_updatesFlow() =
        runTest(testDispatcher) {
            repository.onboardingCompleted.test {
                assertEquals(false, awaitItem())
                repository.setOnboardingCompleted(true)
                assertEquals(true, awaitItem())
            }
        }
}
