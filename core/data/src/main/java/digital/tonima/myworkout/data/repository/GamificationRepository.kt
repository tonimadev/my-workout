package digital.tonima.myworkout.data.repository

import digital.tonima.myworkout.data.local.AchievementDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.AchievementEntity
import digital.tonima.myworkout.data.preferences.GamificationStats
import digital.tonima.myworkout.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

interface GamificationRepository {
    fun getGamificationStats(): Flow<GamificationStats>

    fun getAchievements(): Flow<List<AchievementEntity>>

    suspend fun processSessionCompletion(sessionId: Long)

    fun calculateLevel(totalXp: Int): Int

    fun getXpForNextLevel(currentLevel: Int): Int
}

@Singleton
class GamificationRepositoryImpl
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
        private val sessionDao: WorkoutSessionDao,
        private val achievementDao: AchievementDao,
    ) : GamificationRepository {
        override fun getGamificationStats(): Flow<GamificationStats> = preferencesRepository.gamificationStats

        override fun getAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

        override suspend fun processSessionCompletion(sessionId: Long) {
            val sessionWithLogs = sessionDao.getSessionWithLogs(sessionId).first() ?: return
            val session = sessionWithLogs.session
            val logs = sessionWithLogs.logs

            if (logs.isEmpty()) return

            // XP Calculation
            var xpGained = logs.size * 10 // 10 XP per set
            xpGained += 50 // Workout completion bonus

            // Streak Calculation
            val stats = preferencesRepository.gamificationStats.first()
            val lastWorkout = stats.lastWorkoutTimestamp
            val now = System.currentTimeMillis()

            val newStreak = calculateNewStreak(lastWorkout, now, stats.currentStreak)
            if (newStreak > stats.currentStreak) {
                xpGained += 20 // Streak bonus
            }

            val totalXp = stats.totalXp + xpGained
            val newLevel = calculateLevel(totalXp)

            // Update session metadata
            val totalVolume = logs.sumOf { it.actualWeight * it.actualReps }
            sessionDao.updateSession(
                session.copy(
                    xpGained = xpGained,
                    totalVolume = totalVolume,
                ),
            )

            // Update preferences
            preferencesRepository.updateGamificationStats(
                xpToAdd = xpGained,
                newLevel = newLevel,
                newStreak = newStreak,
                timestamp = now,
            )

            // Check for achievements
            checkForAchievements(newStreak, totalVolume, logs.size)
        }

        private fun calculateNewStreak(
            lastTimestamp: Long,
            currentTimestamp: Long,
            currentStreak: Int,
        ): Int {
            if (lastTimestamp == 0L) return 1

            val diff = currentTimestamp - lastTimestamp
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                days == 0L -> currentStreak // Same day, keep streak
                days == 1L -> currentStreak + 1 // Consecutive day
                else -> 1 // Streak broken
            }
        }

        override fun calculateLevel(totalXp: Int): Int {
            // Level 1: 0 XP
            // Level 2: 100 XP
            // Level 3: 400 XP...
            return (sqrt(totalXp.toDouble() / 100)).toInt() + 1
        }

        override fun getXpForNextLevel(currentLevel: Int): Int {
            return (currentLevel * currentLevel) * 100
        }

        private suspend fun checkForAchievements(
            streak: Int,
            volume: Double,
            setsCount: Int,
        ) {
            val now = System.currentTimeMillis()

            // Streak achievements
            if (streak == 7) {
                achievementDao.insertAchievement(
                    AchievementEntity(
                        type = "STREAK",
                        name = "Guerreiro de Elite",
                        description = "Treinou por 7 dias seguidos!",
                        timestamp = now,
                        level = 1,
                    ),
                )
            }

            // Volume achievements
            if (volume >= 10000.0) {
                achievementDao.insertAchievement(
                    AchievementEntity(
                        type = "VOLUME",
                        name = "Levantador de Peso",
                        description = "Moveu mais de 10 toneladas em um único treino!",
                        timestamp = now,
                        level = 2,
                    ),
                )
            }

            // Intensity achievements
            if (setsCount >= 20) {
                achievementDao.insertAchievement(
                    AchievementEntity(
                        type = "INTENSITY",
                        name = "Máquina de Repetições",
                        description = "Completou mais de 20 séries em um treino!",
                        timestamp = now,
                        level = 1,
                    ),
                )
            }
        }
    }
