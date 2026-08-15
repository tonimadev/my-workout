package digital.tonima.myworkout.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private object PreferencesKeys {
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            val TOTAL_XP = intPreferencesKey("total_xp")
            val CURRENT_LEVEL = intPreferencesKey("current_level")
            val CURRENT_STREAK = intPreferencesKey("current_streak")
            val LAST_WORKOUT_TIMESTAMP = longPreferencesKey("last_workout_timestamp")
        }

        val onboardingCompleted: Flow<Boolean> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
                }

        val gamificationStats: Flow<GamificationStats> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    GamificationStats(
                        totalXp = preferences[PreferencesKeys.TOTAL_XP] ?: 0,
                        currentLevel = preferences[PreferencesKeys.CURRENT_LEVEL] ?: 1,
                        currentStreak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0,
                        lastWorkoutTimestamp = preferences[PreferencesKeys.LAST_WORKOUT_TIMESTAMP] ?: 0L,
                    )
                }

        suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        }

        suspend fun updateGamificationStats(
            xpToAdd: Int,
            newLevel: Int,
            newStreak: Int,
            timestamp: Long,
        ) {
            dataStore.edit { preferences ->
                val currentXp = preferences[PreferencesKeys.TOTAL_XP] ?: 0
                preferences[PreferencesKeys.TOTAL_XP] = currentXp + xpToAdd
                preferences[PreferencesKeys.CURRENT_LEVEL] = newLevel
                preferences[PreferencesKeys.CURRENT_STREAK] = newStreak
                preferences[PreferencesKeys.LAST_WORKOUT_TIMESTAMP] = timestamp
            }
        }
    }

data class GamificationStats(
    val totalXp: Int,
    val currentLevel: Int,
    val currentStreak: Int,
    val lastWorkoutTimestamp: Long,
)
