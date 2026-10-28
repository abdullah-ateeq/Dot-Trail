package com.example.dottrail.model

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

enum class GameState {
    READY,
    COUNTDOWN,
    PLAYING,
    WON,
    GAME_OVER,
    PAUSED
}

enum class AppTheme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class TimerStyle(val displayName: String) {
    DIGITAL("Digital"),
    COMPACT("Compact")
}

data class DotTarget(
    val id: Int,
    val number: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val completed: Boolean = false
)

data class LevelConfig(
    val level: Int,
    val dotCount: Int,
    val timeLimitMs: Long,
    val dotRadiusDp: Float,
    val wrongTapPenaltyMs: Long
)

data class GameResult(
    val level: Int,
    val difficulty: Difficulty,
    val totalDots: Int,
    val completedDots: Int,
    val completionTimeMs: Long?,
    val timeLimitMs: Long,
    val remainingTimeMs: Long,
    val mistakes: Int,
    val score: Int,
    val won: Boolean,
    val isNewRecordScore: Boolean = false,
    val isNewRecordTime: Boolean = false
) {
    val accuracyPercentage: Float
        get() {
            val totalAttempts = totalDots + mistakes
            return if (totalAttempts > 0) {
                (completedDots.toFloat() / totalAttempts.toFloat()) * 100f
            } else 0f
        }
}

enum class PerformanceRating(val title: String, val stars: Int, val description: String) {
    PERFECT("PERFECT", 5, "Flawless run with zero mistakes!"),
    EXCELLENT("EXCELLENT", 5, "Lightning fast and razor sharp!"),
    GREAT("GREAT", 4, "Strong visual scan and reaction speed!"),
    GOOD("GOOD", 3, "Solid round. Keep building that streak!"),
    KEEP_PRACTICING("KEEP PRACTICING", 2, "Stay focused on the sequence and try again!")
}
