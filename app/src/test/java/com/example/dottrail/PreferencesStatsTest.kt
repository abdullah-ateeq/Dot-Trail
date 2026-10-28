package com.example.dottrail

import com.example.dottrail.data.GameStats
import com.example.dottrail.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesStatsTest {

    @Test
    fun testStreakTracking() {
        var stats = GameStats()
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)

        // Win 1
        stats = stats.copy(currentStreak = 1, longestStreak = 1, gamesWon = 1, gamesPlayed = 1)
        // Win 2
        stats = stats.copy(currentStreak = 2, longestStreak = 2, gamesWon = 2, gamesPlayed = 2)
        // Win 3
        stats = stats.copy(currentStreak = 3, longestStreak = 3, gamesWon = 3, gamesPlayed = 3)
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)

        // Loss resets current streak, preserves longest streak
        stats = stats.copy(currentStreak = 0, gamesPlayed = 4)
        assertEquals(0, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun testDifficultyScoreLookup() {
        val stats = GameStats(
            bestScoreEasy = 1200,
            bestScoreMedium = 1650,
            bestScoreHard = 2100,
            bestTimeEasyMs = 14200L,
            bestTimeMediumMs = 18500L,
            bestTimeHardMs = 19800L
        )

        assertEquals(1200, stats.getBestScore(Difficulty.EASY))
        assertEquals(1650, stats.getBestScore(Difficulty.MEDIUM))
        assertEquals(2100, stats.getBestScore(Difficulty.HARD))

        assertEquals(14200L, stats.getBestTimeMs(Difficulty.EASY))
        assertEquals(18500L, stats.getBestTimeMs(Difficulty.MEDIUM))
        assertEquals(19800L, stats.getBestTimeMs(Difficulty.HARD))
    }

    @Test
    fun testWinRateAndAverages() {
        val stats = GameStats(
            gamesPlayed = 10,
            gamesWon = 8,
            totalMistakes = 15,
            bestScoreEasy = 1000,
            bestScoreMedium = 1200,
            bestScoreHard = 1400
        )

        assertEquals(80f, stats.winRatePercentage, 0.01f)
        assertEquals(1.5f, stats.averageMistakes, 0.01f)
        assertEquals(1200, stats.averageScore)
    }
}
