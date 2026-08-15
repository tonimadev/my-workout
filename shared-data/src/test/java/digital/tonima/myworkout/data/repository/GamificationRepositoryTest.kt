package digital.tonima.myworkout.data.repository

import digital.tonima.myworkout.data.local.AchievementDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.model.SessionWithLogs
import digital.tonima.myworkout.data.model.WorkoutSessionEntity
import digital.tonima.myworkout.data.preferences.GamificationStats
import digital.tonima.myworkout.data.preferences.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GamificationRepositoryTest {
    private lateinit var gamificationRepository: GamificationRepository
    private val preferencesRepository: UserPreferencesRepository = mockk()
    private val sessionDao: WorkoutSessionDao = mockk()
    private val achievementDao: AchievementDao = mockk()

    @Before
    fun setUp() {
        gamificationRepository =
            GamificationRepositoryImpl(
                preferencesRepository,
                sessionDao,
                achievementDao,
            )
    }

    @Test
    fun `calculateLevel returns correct levels`() {
        assertEquals(1, gamificationRepository.calculateLevel(0))
        assertEquals(1, gamificationRepository.calculateLevel(50))
        assertEquals(2, gamificationRepository.calculateLevel(100))
        assertEquals(2, gamificationRepository.calculateLevel(399))
        assertEquals(3, gamificationRepository.calculateLevel(400))
    }

    @Test
    fun `processSessionCompletion updates XP and stats correctly`() =
        runTest {
            val sessionId = 1L
            val sessionWithLogs =
                SessionWithLogs(
                    session = WorkoutSessionEntity(id = sessionId, workoutId = 1, startTime = 0L),
                    logs = listOf(mockk(relaxed = true), mockk(relaxed = true)), // 2 logs = 20 XP + 50 XP bonus = 70 XP
                )

            coEvery { sessionDao.getSessionWithLogs(sessionId) } returns flowOf(sessionWithLogs)
            coEvery { preferencesRepository.gamificationStats } returns flowOf(GamificationStats(0, 1, 0, 0L))
            coEvery { sessionDao.updateSession(any()) } returns Unit
            coEvery { preferencesRepository.updateGamificationStats(any(), any(), any(), any()) } returns Unit

            gamificationRepository.processSessionCompletion(sessionId)

            coVerify {
                preferencesRepository.updateGamificationStats(
                    xpToAdd = 70,
                    newLevel = 1, // sqrt(70/100) + 1 = 0 + 1 = 1
                    newStreak = 1,
                    timestamp = any(),
                )
            }
        }
}
