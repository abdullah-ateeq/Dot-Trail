package com.example.dottrail

import com.example.dottrail.game.LevelGenerator
import com.example.dottrail.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class LevelGeneratorTest {

    @Test
    fun testProgressionLevel1To3Easy() {
        val lvl1 = LevelGenerator.createLevelConfig(Difficulty.EASY, 1)
        val lvl2 = LevelGenerator.createLevelConfig(Difficulty.EASY, 2)
        val lvl3 = LevelGenerator.createLevelConfig(Difficulty.EASY, 3)

        assertEquals(8, lvl1.dotCount)
        assertEquals(8, lvl2.dotCount)
        assertEquals(9, lvl3.dotCount)

        assertTrue(lvl1.timeLimitMs >= lvl2.timeLimitMs)
        assertTrue(lvl2.timeLimitMs >= lvl3.timeLimitMs)
        assertEquals(500L, lvl1.wrongTapPenaltyMs)
    }

    @Test
    fun testDifficultyModesBaseValues() {
        val easy = LevelGenerator.createLevelConfig(Difficulty.EASY, 1)
        val med = LevelGenerator.createLevelConfig(Difficulty.MEDIUM, 1)
        val hard = LevelGenerator.createLevelConfig(Difficulty.HARD, 1)

        assertEquals(8, easy.dotCount)
        assertEquals(12, med.dotCount)
        assertEquals(18, hard.dotCount)

        assertEquals(25_000L, easy.timeLimitMs)
        assertEquals(22_000L, med.timeLimitMs)
        assertEquals(20_000L, hard.timeLimitMs)

        assertEquals(500L, easy.wrongTapPenaltyMs)
        assertEquals(1000L, med.wrongTapPenaltyMs)
        assertEquals(1500L, hard.wrongTapPenaltyMs)
    }

    @Test
    fun testMaxDotsCappedAt30() {
        val config = LevelGenerator.createLevelConfig(Difficulty.HARD, 100)
        assertEquals(30, config.dotCount)
        assertTrue(config.dotCount <= LevelGenerator.MAX_DOTS)
    }

    @Test
    fun testMinTimeLimitRespected() {
        val config = LevelGenerator.createLevelConfig(Difficulty.EASY, 50)
        assertTrue(config.timeLimitMs >= LevelGenerator.MIN_TIME_LIMIT_MS)
    }

    @Test
    fun testDotPlacementNoOutsideAndNoOverlap() {
        val config = LevelGenerator.createLevelConfig(Difficulty.HARD, 5) // ~20 dots
        val boardWidth = 1080f
        val boardHeight = 1920f
        val density = 2.5f

        val dots = LevelGenerator.generateDots(
            config = config,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            density = density,
            seed = 12345L
        )

        assertEquals(config.dotCount, dots.size)

        // Verify valid sequence
        val numbers = dots.map { it.number }.sorted()
        assertEquals((1..config.dotCount).toList(), numbers)

        // Verify within board boundaries
        for (dot in dots) {
            assertFalse(dot.x.isNaN())
            assertFalse(dot.y.isNaN())
            assertTrue("Dot X inside board", dot.x >= dot.radius && dot.x <= boardWidth - dot.radius)
            assertTrue("Dot Y inside board", dot.y >= dot.radius && dot.y <= boardHeight - dot.radius)
        }

        // Verify no overlapping centers (minimum distance >= visualRadius)
        for (i in dots.indices) {
            for (j in i + 1 until dots.size) {
                val d1 = dots[i]
                val d2 = dots[j]
                val dx = d1.x - d2.x
                val dy = d1.y - d2.y
                val dist = sqrt(dx * dx + dy * dy)
                val minSeparation = d1.radius + d2.radius
                assertTrue(
                    "Dots $i and $j overlap! dist=$dist, min=$minSeparation",
                    dist >= minSeparation * 0.95f
                )
            }
        }
    }

    @Test
    fun testDeterministicPlacementWithSeed() {
        val config = LevelGenerator.createLevelConfig(Difficulty.MEDIUM, 2)
        val dots1 = LevelGenerator.generateDots(config, 800f, 1200f, 2f, seed = 9999L)
        val dots2 = LevelGenerator.generateDots(config, 800f, 1200f, 2f, seed = 9999L)

        assertEquals(dots1.size, dots2.size)
        for (i in dots1.indices) {
            assertEquals(dots1[i].x, dots2[i].x, 0.001f)
            assertEquals(dots1[i].y, dots2[i].y, 0.001f)
        }
    }
}
