package com.example.dottrail.game

import com.example.dottrail.model.Difficulty
import com.example.dottrail.model.DotTarget
import com.example.dottrail.model.LevelConfig
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

object LevelGenerator {

    const val MAX_DOTS = 30
    const val MIN_TIME_LIMIT_MS = 12_000L

    fun createLevelConfig(difficulty: Difficulty, level: Int): LevelConfig {
        val safeLevel = max(1, level)
        val (baseDots, baseTimeSec, baseRadiusDp, penaltyMs) = when (difficulty) {
            Difficulty.EASY -> Quad(8, 25, 28f, 500L)
            Difficulty.MEDIUM -> Quad(12, 22, 25f, 1000L)
            Difficulty.HARD -> Quad(18, 20, 22f, 1500L)
        }

        // Increase dots gradually: +1 dot every 2 levels
        val addedDots = (safeLevel - 1) / 2
        val dotCount = min(MAX_DOTS, baseDots + addedDots)

        // Time reduction: 1 second reduction every 1-2 levels, capped at minimum time limit
        val timeDeductionSec = when (difficulty) {
            Difficulty.EASY -> (safeLevel - 1) * 0.75f
            Difficulty.MEDIUM -> (safeLevel - 1) * 0.85f
            Difficulty.HARD -> (safeLevel - 1) * 1.0f
        }
        val calculatedTimeSec = max(
            if (difficulty == Difficulty.HARD) 10f else (MIN_TIME_LIMIT_MS / 1000f),
            baseTimeSec - timeDeductionSec
        )
        val timeLimitMs = (calculatedTimeSec * 1000).toLong()

        // Slightly decrease radius as dot count grows, maintaining minimum comfortable touch size
        val dotDensityFactor = (dotCount - baseDots).toFloat() / (MAX_DOTS - baseDots).coerceAtLeast(1)
        val radiusReduction = dotDensityFactor * 4f // up to 4dp smaller
        val dotRadiusDp = max(20f, baseRadiusDp - radiusReduction)

        return LevelConfig(
            level = safeLevel,
            dotCount = dotCount,
            timeLimitMs = timeLimitMs,
            dotRadiusDp = dotRadiusDp,
            wrongTapPenaltyMs = penaltyMs
        )
    }

    /**
     * Generates fair, non-overlapping dot positions within the playable board.
     */
    fun generateDots(
        config: LevelConfig,
        boardWidth: Float,
        boardHeight: Float,
        density: Float,
        seed: Long = Random.nextLong()
    ): List<DotTarget> {
        val random = Random(seed)
        val visualRadiusPx = config.dotRadiusDp * density

        val marginX = visualRadiusPx + (16f * density)
        val marginY = visualRadiusPx + (16f * density)
        val marginYBottom = visualRadiusPx + (24f * density)

        val minX = marginX
        val maxX = max(minX + visualRadiusPx, boardWidth - marginX)
        val minY = marginY
        val maxY = max(minY + visualRadiusPx, boardHeight - marginYBottom)

        val baseMinDistance = max(boardWidth * 0.12f, visualRadiusPx * 2.3f)
        var minDistance = baseMinDistance

        val totalDots = config.dotCount
        var placedDots: MutableList<DotTarget> = mutableListOf()
        var generationAttempts = 0
        val maxGenerationAttempts = 40

        while (generationAttempts < maxGenerationAttempts) {
            placedDots = mutableListOf()
            var allPlaced = true

            for (num in 1..totalDots) {
                var placed = false
                var attempts = 0
                val maxAttemptsPerDot = 150

                while (attempts < maxAttemptsPerDot) {
                    val candidateX = minX + random.nextFloat() * (maxX - minX)
                    val candidateY = minY + random.nextFloat() * (maxY - minY)

                    // Check distance against all previously placed dots
                    var tooClose = false
                    for (dot in placedDots) {
                        val dx = candidateX - dot.x
                        val dy = candidateY - dot.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < minDistance) {
                            tooClose = true
                            break
                        }
                    }

                    if (!tooClose) {
                        placedDots.add(
                            DotTarget(
                                id = num,
                                number = num,
                                x = candidateX,
                                y = candidateY,
                                radius = visualRadiusPx,
                                completed = false
                            )
                        )
                        placed = true
                        break
                    }
                    attempts++
                }

                if (!placed) {
                    allPlaced = false
                    break
                }
            }

            if (allPlaced && placedDots.size == totalDots) {
                return placedDots
            }

            // If it failed to place all dots, slightly relax the min distance and retry
            generationAttempts++
            minDistance = max(visualRadiusPx * 2.05f, minDistance * 0.94f)
        }

        // Fallback: Grid layout with subtle jitter to guarantee 100% fair placement even on tiny screens
        return generateFallbackGrid(totalDots, minX, maxX, minY, maxY, visualRadiusPx, random)
    }

    private fun generateFallbackGrid(
        count: Int,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        radius: Float,
        random: Random
    ): List<DotTarget> {
        val dots = mutableListOf<DotTarget>()
        val cols = kotlin.math.ceil(sqrt(count * (maxX - minX) / (maxY - minY))).toInt().coerceAtLeast(2)
        val rows = kotlin.math.ceil(count.toDouble() / cols).toInt().coerceAtLeast(2)

        val cellW = (maxX - minX) / cols
        val cellH = (maxY - minY) / rows

        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                cells.add(Pair(r, c))
            }
        }
        cells.shuffle(random)

        for (i in 0 until count) {
            val (r, c) = cells[i]
            val jitterX = (random.nextFloat() - 0.5f) * (cellW - radius * 2f).coerceAtLeast(0f) * 0.6f
            val jitterY = (random.nextFloat() - 0.5f) * (cellH - radius * 2f).coerceAtLeast(0f) * 0.6f
            val posX = (minX + c * cellW + cellW / 2f + jitterX).coerceIn(minX, maxX)
            val posY = (minY + r * cellH + cellH / 2f + jitterY).coerceIn(minY, maxY)

            dots.add(
                DotTarget(
                    id = i + 1,
                    number = i + 1,
                    x = posX,
                    y = posY,
                    radius = radius,
                    completed = false
                )
            )
        }
        return dots
    }

    private data class Quad(val baseDots: Int, val baseTimeSec: Int, val radiusDp: Float, val penaltyMs: Long)
}
