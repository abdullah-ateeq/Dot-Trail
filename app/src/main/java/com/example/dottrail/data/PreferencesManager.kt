package com.example.dottrail.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dottrail.model.AppTheme
import com.example.dottrail.model.Difficulty
import com.example.dottrail.model.TimerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameStats(
    val bestScoreEasy: Int = 0,
    val bestScoreMedium: Int = 0,
    val bestScoreHard: Int = 0,
    val bestTimeEasyMs: Long = 0L,
    val bestTimeMediumMs: Long = 0L,
    val bestTimeHardMs: Long = 0L,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val highestLevel: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalMistakes: Int = 0
) {
    fun getBestScore(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> bestScoreEasy
        Difficulty.MEDIUM -> bestScoreMedium
        Difficulty.HARD -> bestScoreHard
    }

    fun getBestTimeMs(difficulty: Difficulty): Long = when (difficulty) {
        Difficulty.EASY -> bestTimeEasyMs
        Difficulty.MEDIUM -> bestTimeMediumMs
        Difficulty.HARD -> bestTimeHardMs
    }

    val averageScore: Int
        get() = if (gamesWon > 0) {
            val totalScore = (bestScoreEasy + bestScoreMedium + bestScoreHard)
            totalScore / 3
        } else 0

    val winRatePercentage: Float
        get() = if (gamesPlayed > 0) (gamesWon.toFloat() / gamesPlayed.toFloat()) * 100f else 0f

    val averageMistakes: Float
        get() = if (gamesPlayed > 0) totalMistakes.toFloat() / gamesPlayed.toFloat() else 0f
}

data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val timerStyle: TimerStyle = TimerStyle.DIGITAL,
    val reduceMotion: Boolean = false
)

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<GameStats> = _statsFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadStats(): GameStats {
        return GameStats(
            bestScoreEasy = prefs.getInt(KEY_BEST_SCORE_EASY, 0),
            bestScoreMedium = prefs.getInt(KEY_BEST_SCORE_MEDIUM, 0),
            bestScoreHard = prefs.getInt(KEY_BEST_SCORE_HARD, 0),
            bestTimeEasyMs = prefs.getLong(KEY_BEST_TIME_EASY, 0L),
            bestTimeMediumMs = prefs.getLong(KEY_BEST_TIME_MEDIUM, 0L),
            bestTimeHardMs = prefs.getLong(KEY_BEST_TIME_HARD, 0L),
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            gamesWon = prefs.getInt(KEY_GAMES_WON, 0),
            highestLevel = prefs.getInt(KEY_HIGHEST_LEVEL, 1),
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0),
            totalMistakes = prefs.getInt(KEY_TOTAL_MISTAKES, 0)
        )
    }

    private fun loadSettings(): UserSettings {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val timerStyleName = prefs.getString(KEY_TIMER_STYLE, TimerStyle.DIGITAL.name) ?: TimerStyle.DIGITAL.name
        return UserSettings(
            theme = runCatching { AppTheme.valueOf(themeName) }.getOrDefault(AppTheme.SYSTEM),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            timerStyle = runCatching { TimerStyle.valueOf(timerStyleName) }.getOrDefault(TimerStyle.DIGITAL),
            reduceMotion = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        )
    }

    fun recordGameFinished(
        difficulty: Difficulty,
        level: Int,
        won: Boolean,
        score: Int,
        completionTimeMs: Long?,
        mistakes: Int
    ): Pair<Boolean, Boolean> {
        val current = _statsFlow.value
        val newGamesPlayed = current.gamesPlayed + 1
        val newGamesWon = if (won) current.gamesWon + 1 else current.gamesWon
        val newStreak = if (won) current.currentStreak + 1 else 0
        val newLongestStreak = kotlin.math.max(current.longestStreak, newStreak)
        val newHighestLevel = if (won) kotlin.math.max(current.highestLevel, level + 1) else current.highestLevel
        val newTotalMistakes = current.totalMistakes + mistakes

        var isNewRecordScore = false
        var isNewRecordTime = false

        var easyScore = current.bestScoreEasy
        var medScore = current.bestScoreMedium
        var hardScore = current.bestScoreHard

        var easyTime = current.bestTimeEasyMs
        var medTime = current.bestTimeMediumMs
        var hardTime = current.bestTimeHardMs

        if (won) {
            when (difficulty) {
                Difficulty.EASY -> {
                    if (score > easyScore) {
                        easyScore = score
                        isNewRecordScore = true
                    }
                    if (completionTimeMs != null && (easyTime == 0L || completionTimeMs < easyTime)) {
                        easyTime = completionTimeMs
                        isNewRecordTime = true
                    }
                }
                Difficulty.MEDIUM -> {
                    if (score > medScore) {
                        medScore = score
                        isNewRecordScore = true
                    }
                    if (completionTimeMs != null && (medTime == 0L || completionTimeMs < medTime)) {
                        medTime = completionTimeMs
                        isNewRecordTime = true
                    }
                }
                Difficulty.HARD -> {
                    if (score > hardScore) {
                        hardScore = score
                        isNewRecordScore = true
                    }
                    if (completionTimeMs != null && (hardTime == 0L || completionTimeMs < hardTime)) {
                        hardTime = completionTimeMs
                        isNewRecordTime = true
                    }
                }
            }
        }

        prefs.edit()
            .putInt(KEY_BEST_SCORE_EASY, easyScore)
            .putInt(KEY_BEST_SCORE_MEDIUM, medScore)
            .putInt(KEY_BEST_SCORE_HARD, hardScore)
            .putLong(KEY_BEST_TIME_EASY, easyTime)
            .putLong(KEY_BEST_TIME_MEDIUM, medTime)
            .putLong(KEY_BEST_TIME_HARD, hardTime)
            .putInt(KEY_GAMES_PLAYED, newGamesPlayed)
            .putInt(KEY_GAMES_WON, newGamesWon)
            .putInt(KEY_HIGHEST_LEVEL, newHighestLevel)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putInt(KEY_LONGEST_STREAK, newLongestStreak)
            .putInt(KEY_TOTAL_MISTAKES, newTotalMistakes)
            .apply()

        _statsFlow.value = loadStats()
        return Pair(isNewRecordScore, isNewRecordTime)
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(theme = theme)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(hapticsEnabled = enabled)
    }

    fun setTimerStyle(style: TimerStyle) {
        prefs.edit().putString(KEY_TIMER_STYLE, style.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(timerStyle = style)
    }

    fun setReduceMotion(reduce: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, reduce).apply()
        _settingsFlow.value = _settingsFlow.value.copy(reduceMotion = reduce)
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE_EASY, 0)
            .putInt(KEY_BEST_SCORE_MEDIUM, 0)
            .putInt(KEY_BEST_SCORE_HARD, 0)
            .putLong(KEY_BEST_TIME_EASY, 0L)
            .putLong(KEY_BEST_TIME_MEDIUM, 0L)
            .putLong(KEY_BEST_TIME_HARD, 0L)
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_GAMES_WON, 0)
            .putInt(KEY_HIGHEST_LEVEL, 1)
            .putInt(KEY_CURRENT_STREAK, 0)
            .putInt(KEY_LONGEST_STREAK, 0)
            .putInt(KEY_TOTAL_MISTAKES, 0)
            .apply()
        _statsFlow.value = loadStats()
    }

    companion object {
        private const val PREFS_NAME = "dot_trail_prefs"

        private const val KEY_BEST_SCORE_EASY = "best_score_easy"
        private const val KEY_BEST_SCORE_MEDIUM = "best_score_med"
        private const val KEY_BEST_SCORE_HARD = "best_score_hard"

        private const val KEY_BEST_TIME_EASY = "best_time_easy"
        private const val KEY_BEST_TIME_MEDIUM = "best_time_med"
        private const val KEY_BEST_TIME_HARD = "best_time_hard"

        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_GAMES_WON = "games_won"
        private const val KEY_HIGHEST_LEVEL = "highest_level"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_TOTAL_MISTAKES = "total_mistakes"

        private const val KEY_THEME = "app_theme"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_TIMER_STYLE = "timer_style"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
    }
}
