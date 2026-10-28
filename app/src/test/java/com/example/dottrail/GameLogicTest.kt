package com.example.dottrail

import com.example.dottrail.game.LevelGenerator
import com.example.dottrail.model.Difficulty
import com.example.dottrail.model.DotTarget
import com.example.dottrail.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max

class GameLogicTest {

    // Simulation of game logic to test state machine transitions deterministically
    private class SimulatedGame(val totalDots: Int, val timeLimitMs: Long, val penaltyMs: Long) {
        var currentTarget = 1
        var mistakes = 0
        var remainingTimeMs = timeLimitMs
        var gameState = GameState.PLAYING
        val completedDots = mutableListOf<Int>()

        fun tapDot(number: Int) {
            if (gameState != GameState.PLAYING) return

            if (number == currentTarget) {
                completedDots.add(number)
                currentTarget++
                if (currentTarget > totalDots) {
                    gameState = GameState.WON
                }
            } else {
                mistakes++
                remainingTimeMs = max(0L, remainingTimeMs - penaltyMs)
                if (remainingTimeMs <= 0L) {
                    gameState = GameState.GAME_OVER
                }
            }
        }

        fun onTimeout() {
            remainingTimeMs = 0L
            gameState = GameState.GAME_OVER
        }
    }

    @Test
    fun testSequenceTest() {
        val game = SimulatedGame(totalDots = 5, timeLimitMs = 20_000L, penaltyMs = 1000L)
        assertEquals(1, game.currentTarget)

        // After tapping 1
        game.tapDot(1)
        assertEquals(2, game.currentTarget)
        assertTrue(game.completedDots.contains(1))

        // After tapping 2
        game.tapDot(2)
        assertEquals(3, game.currentTarget)
        assertTrue(game.completedDots.contains(2))
    }

    @Test
    fun testWrongTapTest() {
        val game = SimulatedGame(totalDots = 5, timeLimitMs = 20_000L, penaltyMs = 1000L)
        game.tapDot(1)
        game.tapDot(2)
        assertEquals(3, game.currentTarget)

        // Current target is 3. Player taps 5
        game.tapDot(5)

        // Expected: currentTarget remains 3
        assertEquals(3, game.currentTarget)
        // Mistakes increases by 1
        assertEquals(1, game.mistakes)
        // Remaining time decreases by penalty
        assertEquals(19_000L, game.remainingTimeMs)
        // Game still in playing state
        assertEquals(GameState.PLAYING, game.gameState)
    }

    @Test
    fun testWinTest() {
        val game = SimulatedGame(totalDots = 5, timeLimitMs = 20_000L, penaltyMs = 1000L)

        // Tap 1, 2, 3, 4, 5
        game.tapDot(1)
        game.tapDot(2)
        game.tapDot(3)
        game.tapDot(4)
        assertEquals(GameState.PLAYING, game.gameState)

        game.tapDot(5)
        // Expected: GameState.WON
        assertEquals(GameState.WON, game.gameState)
        assertEquals(5, game.completedDots.size)
    }

    @Test
    fun testTimeoutTest() {
        val game = SimulatedGame(totalDots = 5, timeLimitMs = 20_000L, penaltyMs = 1000L)
        game.onTimeout()

        assertEquals(0L, game.remainingTimeMs)
        assertEquals(GameState.GAME_OVER, game.gameState)

        // Further taps ignored
        game.tapDot(1)
        assertEquals(1, game.currentTarget)
        assertTrue(game.completedDots.isEmpty())
    }

    @Test
    fun testWrongTapCausesTimeoutIfPenaltyDepletesTime() {
        val game = SimulatedGame(totalDots = 5, timeLimitMs = 800L, penaltyMs = 1000L)
        assertEquals(GameState.PLAYING, game.gameState)

        game.tapDot(4) // wrong tap when target is 1
        assertEquals(0L, game.remainingTimeMs)
        assertEquals(GameState.GAME_OVER, game.gameState)
    }
}
