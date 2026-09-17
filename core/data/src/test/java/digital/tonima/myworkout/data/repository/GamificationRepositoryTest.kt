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
import java.time.LocalDate
import java.time.ZoneId

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
                    workout = null,
                    logs = listOf(mockk(relaxed = true), mockk(relaxed = true)), // 2 logs = 20 XP + 50 XP bonus = 70 XP
                )

            coEvery { sessionDao.getSessionWithLogs(sessionId) } returns flowOf(sessionWithLogs)
            coEvery { preferencesRepository.gamificationStats } returns flowOf(GamificationStats(0, 1, 0, 0L))
            coEvery { sessionDao.updateSession(any()) } returns Unit
            coEvery { preferencesRepository.updateGamificationStats(any(), any(), any(), any()) } returns Unit

            gamificationRepository.processSessionCompletion(sessionId)

            coVerify {
                preferencesRepository.updateGamificationStats(
                    xpToAdd = 90,
                    newLevel = 1, // sqrt(90/100) + 1 = 0 + 1 = 1
                    newStreak = 1,
                    timestamp = any(),
                )
            }
        }

    @Test
    fun `calculateNewStreak increments when the new session crosses into the next calendar day`() {
        // Last workout was late at night on day 1; this one is shortly after midnight on day 2.
        // Real elapsed time is only ~2 hours, but it is a new calendar day, so the streak must
        // still advance (a naive 24h-bucket check would wrongly treat this as "same day").
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2024, 1, 10)
        val day2 = day1.plusDays(1)
        val lastWorkout = day1.atTime(23, 0).atZone(zone).toInstant().toEpochMilli()
        val now = day2.atTime(1, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(4, calculateNewStreak(lastWorkout, now, currentStreak = 3))
    }

    @Test
    fun `calculateNewStreak keeps the same streak for a second workout on the same calendar day`() {
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2024, 1, 10)
        val morning = day1.atTime(6, 0).atZone(zone).toInstant().toEpochMilli()
        val night = day1.atTime(23, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(3, calculateNewStreak(morning, night, currentStreak = 3))
    }

    @Test
    fun `calculateNewStreak resets when a full calendar day is skipped`() {
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2024, 1, 10)
        val day3 = day1.plusDays(2)
        val lastWorkout = day1.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val now = day3.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(1, calculateNewStreak(lastWorkout, now, currentStreak = 5))
    }

    @Test
    fun `calculateNewStreak starts at 1 for the very first workout`() {
        assertEquals(1, calculateNewStreak(lastTimestamp = 0L, currentTimestamp = 123456789L, currentStreak = 0))
    }
}
