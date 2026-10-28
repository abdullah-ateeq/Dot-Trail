package com.example.dottrail

import com.example.dottrail.game.ScoreCalculator
import com.example.dottrail.model.Difficulty
import com.example.dottrail.model.GameResult
import com.example.dottrail.model.PerformanceRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun testScoreFormulaDocumentedExample() {
        // From requirements:
        // 12 dots:
        // Base: 1200
        // Remaining: 8.4 seconds -> Speed: 420
        // Mistakes: 2 -> Penalty: 100
        // Final: 1520
        val breakdown = ScoreCalculator.calculate(
            totalDots = 12,
            remainingTimeMs = 8_400L,
            mistakes = 2,
            won = true
        )
        assertEquals(1200, breakdown.baseScore)
        assertEquals(420, breakdown.speedBonus)
        assertEquals(100, breakdown.mistakePenalty)
        assertEquals(1520, breakdown.finalScore)
    }

    @Test
    fun testScoreFormulaSpecificationTest() {
        // Given in prompt:
        // totalDots = 10
        // remainingTime = 5 sec
        // mistakes = 1
        // Base = 1000, Speed = 250, Penalty = 50 -> Final = 1200
        val breakdown = ScoreCalculator.calculate(
            totalDots = 10,
            remainingTimeMs = 5_000L,
            mistakes = 1,
            won = true
        )
        assertEquals(1000, breakdown.baseScore)
        assertEquals(250, breakdown.speedBonus)
        assertEquals(50, breakdown.mistakePenalty)
        assertEquals(1200, breakdown.finalScore)
    }

    @Test
    fun testScoreNeverNegative() {
        val breakdown = ScoreCalculator.calculate(
            totalDots = 5,
            remainingTimeMs = 0L,
            mistakes = 20, // 1000 penalty vs 500 base
            won = true
        )
        assertTrue(breakdown.finalScore >= 0)
        assertEquals(0, breakdown.finalScore)
    }

    @Test
    fun testScoreZeroOnLoss() {
        val breakdown = ScoreCalculator.calculate(
            totalDots = 10,
            remainingTimeMs = 0L,
            mistakes = 2,
            won = false
        )
        assertEquals(0, breakdown.finalScore)
    }

    @Test
    fun testAccuracyCalculation() {
        val result = GameResult(
            level = 1,
            difficulty = Difficulty.EASY,
            totalDots = 10,
            completedDots = 10,
            completionTimeMs = 15000L,
            timeLimitMs = 25000L,
            remainingTimeMs = 10000L,
            mistakes = 2,
            score = 1400,
            won = true
        )
        // accuracy = 10 / (10 + 2) * 100 = 83.333%
        val expected = (10f / 12f) * 100f
        assertEquals(expected, result.accuracyPercentage, 0.01f)
    }

    @Test
    fun testPerformanceRatingPerfect() {
        val rating = ScoreCalculator.getPerformanceRating(
            won = true,
            mistakes = 0,
            remainingTimeMs = 10000L,
            timeLimitMs = 25000L,
            accuracy = 100f
        )
        assertEquals(PerformanceRating.PERFECT, rating)
    }

    @Test
    fun testPerformanceRatingLoss() {
        val rating = ScoreCalculator.getPerformanceRating(
            won = false,
            mistakes = 3,
            remainingTimeMs = 0L,
            timeLimitMs = 25000L,
            accuracy = 70f
        )
        assertEquals(PerformanceRating.KEEP_PRACTICING, rating)
    }
}
