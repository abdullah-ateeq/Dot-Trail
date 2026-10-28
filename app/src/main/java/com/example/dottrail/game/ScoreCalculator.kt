package com.example.dottrail.game

import com.example.dottrail.model.PerformanceRating
import kotlin.math.max

object ScoreCalculator {

    data class ScoreBreakdown(
        val baseScore: Int,
        val speedBonus: Int,
        val mistakePenalty: Int,
        val finalScore: Int
    )

    fun calculate(
        totalDots: Int,
        remainingTimeMs: Long,
        mistakes: Int,
        won: Boolean
    ): ScoreBreakdown {
        if (!won) {
            return ScoreBreakdown(
                baseScore = 0,
                speedBonus = 0,
                mistakePenalty = mistakes * 50,
                finalScore = 0
            )
        }

        val baseScore = totalDots * 100
        val remainingSec = (remainingTimeMs / 1000.0)
        val speedBonus = (remainingSec * 50).toInt().coerceAtLeast(0)
        val mistakePenalty = mistakes * 50
        val finalScore = max(0, baseScore + speedBonus - mistakePenalty)

        return ScoreBreakdown(
            baseScore = baseScore,
            speedBonus = speedBonus,
            mistakePenalty = mistakePenalty,
            finalScore = finalScore
        )
    }

    fun getPerformanceRating(
        won: Boolean,
        mistakes: Int,
        remainingTimeMs: Long,
        timeLimitMs: Long,
        accuracy: Float
    ): PerformanceRating {
        if (!won) return PerformanceRating.KEEP_PRACTICING

        return when {
            mistakes == 0 -> PerformanceRating.PERFECT
            remainingTimeMs >= (timeLimitMs * 0.25) && mistakes <= 1 -> PerformanceRating.EXCELLENT
            accuracy >= 80f || mistakes <= 2 -> PerformanceRating.GREAT
            else -> PerformanceRating.GOOD
        }
    }
}
