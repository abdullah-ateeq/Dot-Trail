package com.example.dottrail.game

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dottrail.audio.SoundEffectManager
import com.example.dottrail.data.GameStats
import com.example.dottrail.data.PreferencesManager
import com.example.dottrail.data.UserSettings
import com.example.dottrail.haptics.HapticFeedbackManager
import com.example.dottrail.model.Difficulty
import com.example.dottrail.model.DotTarget
import com.example.dottrail.model.GameResult
import com.example.dottrail.model.GameState
import com.example.dottrail.model.LevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

data class DotTrailUiState(
    val gameState: GameState = GameState.READY,
    val level: Int = 1,
    val difficulty: Difficulty = Difficulty.EASY,
    val config: LevelConfig = LevelGenerator.createLevelConfig(Difficulty.EASY, 1),
    val dots: List<DotTarget> = emptyList(),
    val currentTarget: Int = 1,
    val mistakes: Int = 0,
    val timeLimitMs: Long = 25_000L,
    val remainingTimeMs: Long = 25_000L,
    val completionTimeMs: Long? = null,
    val score: Int = 0,
    val lastWrongDotId: Int? = null,
    val wrongTapFeedbackTrigger: Long = 0L,
    val lastCorrectDotId: Int? = null,
    val trailAnimationProgress: Float = 1f,
    val countdownNumber: Int = 3, // 3, 2, 1, 0 (0 = "GO!")
    val isWarningTime: Boolean = false,
    val gameResult: GameResult? = null,
    val levelSeed: Long = 0L,
    val showPenaltyPopup: Boolean = false,
    val penaltyAmountSec: Float = 0.5f
)

class DotTrailViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = PreferencesManager(application)
    val soundManager = SoundEffectManager()
    val hapticManager = HapticFeedbackManager(application)

    val statsFlow: StateFlow<GameStats> = preferencesManager.statsFlow
    val settingsFlow: StateFlow<UserSettings> = preferencesManager.settingsFlow

    private val _uiState = MutableStateFlow(DotTrailUiState())
    val uiState: StateFlow<DotTrailUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null
    private var trailAnimJob: Job? = null
    private var penaltyPopupJob: Job? = null

    private var periodStartTimeRealtime: Long = 0L
    private var elapsedUnpausedMs: Long = 0L
    private var accumulatedPenaltyMs: Long = 0L

    private var currentBoardWidth: Float = 0f
    private var currentBoardHeight: Float = 0f
    private var currentDensity: Float = 1f

    init {
        // Synchronize audio and haptics settings with user preferences
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                soundManager.isEnabled = settings.soundEnabled
                hapticManager.isEnabled = settings.hapticsEnabled
            }
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (_uiState.value.difficulty != difficulty) {
            _uiState.update {
                it.copy(
                    difficulty = difficulty,
                    level = 1,
                    config = LevelGenerator.createLevelConfig(difficulty, 1)
                )
            }
        }
    }

    /**
     * Initializes or resizes the playable canvas and triggers level preparation.
     */
    fun onBoardSizeChanged(width: Float, height: Float, density: Float) {
        if (width <= 0f || height <= 0f) return
        val sizeChanged = currentBoardWidth != width || currentBoardHeight != height
        currentBoardWidth = width
        currentBoardHeight = height
        currentDensity = density

        if (_uiState.value.dots.isEmpty() || sizeChanged) {
            startLevel(
                level = _uiState.value.level,
                difficulty = _uiState.value.difficulty,
                regenerateNewLayout = true
            )
        }
    }

    fun startNextLevel() {
        startLevel(
            level = _uiState.value.level + 1,
            difficulty = _uiState.value.difficulty,
            regenerateNewLayout = true
        )
    }

    fun restartCurrentLevel() {
        startLevel(
            level = _uiState.value.level,
            difficulty = _uiState.value.difficulty,
            regenerateNewLayout = true
        )
    }

    fun startLevel(
        level: Int = _uiState.value.level,
        difficulty: Difficulty = _uiState.value.difficulty,
        regenerateNewLayout: Boolean = true
    ) {
        cancelAllJobs()

        val config = LevelGenerator.createLevelConfig(difficulty, level)
        val seed = if (regenerateNewLayout) Random.nextLong() else _uiState.value.levelSeed
        val dots = if (currentBoardWidth > 0f && currentBoardHeight > 0f) {
            LevelGenerator.generateDots(config, currentBoardWidth, currentBoardHeight, currentDensity, seed)
        } else {
            emptyList()
        }

        periodStartTimeRealtime = 0L
        elapsedUnpausedMs = 0L
        accumulatedPenaltyMs = 0L

        _uiState.update {
            it.copy(
                gameState = GameState.COUNTDOWN,
                level = level,
                difficulty = difficulty,
                config = config,
                dots = dots,
                currentTarget = 1,
                mistakes = 0,
                timeLimitMs = config.timeLimitMs,
                remainingTimeMs = config.timeLimitMs,
                completionTimeMs = null,
                score = 0,
                lastWrongDotId = null,
                lastCorrectDotId = null,
                trailAnimationProgress = 1f,
                countdownNumber = 3,
                isWarningTime = false,
                gameResult = null,
                levelSeed = seed,
                showPenaltyPopup = false,
                penaltyAmountSec = config.wrongTapPenaltyMs / 1000f
            )
        }

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            // 3
            _uiState.update { it.copy(countdownNumber = 3) }
            soundManager.playCountdownTick()
            hapticManager.vibrateCountdown()
            delay(700L)

            // 2
            _uiState.update { it.copy(countdownNumber = 2) }
            soundManager.playCountdownTick()
            hapticManager.vibrateCountdown()
            delay(700L)

            // 1
            _uiState.update { it.copy(countdownNumber = 1) }
            soundManager.playCountdownTick()
            hapticManager.vibrateCountdown()
            delay(700L)

            // GO!
            _uiState.update { it.copy(countdownNumber = 0) }
            soundManager.playCountdownGo()
            hapticManager.vibrateGo()
            delay(350L)

            // Gameplay begins
            startGameplayTimer()
        }
    }

    private fun startGameplayTimer() {
        periodStartTimeRealtime = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(gameState = GameState.PLAYING) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val limit = _uiState.value.timeLimitMs
            while (isActive && _uiState.value.gameState == GameState.PLAYING) {
                val now = SystemClock.elapsedRealtime()
                val currentPeriodElapsed = now - periodStartTimeRealtime
                val totalElapsed = currentPeriodElapsed + elapsedUnpausedMs + accumulatedPenaltyMs
                val remaining = max(0L, limit - totalElapsed)
                val isWarning = remaining in 1..5000L

                _uiState.update {
                    it.copy(
                        remainingTimeMs = remaining,
                        isWarningTime = isWarning
                    )
                }

                if (remaining <= 0L) {
                    onTimeExpired()
                    break
                }
                delay(25L)
            }
        }
    }

    /**
     * Hit detection: selects the nearest non-completed dot within touch radius.
     */
    fun onCanvasTouch(touchX: Float, touchY: Float) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return

        val density = currentDensity
        val minHitDiameterPx = 48f * density
        val minHitRadiusPx = minHitDiameterPx / 2f

        var nearestDot: DotTarget? = null
        var minDistance = Float.MAX_VALUE

        for (dot in state.dots) {
            if (dot.completed) continue
            val hitRadius = max(dot.radius, minHitRadiusPx)
            val dx = touchX - dot.x
            val dy = touchY - dot.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= hitRadius && dist < minDistance) {
                nearestDot = dot
                minDistance = dist
            }
        }

        if (nearestDot != null) {
            handleDotTapped(nearestDot)
        }
    }

    private fun handleDotTapped(dot: DotTarget) {
        val state = _uiState.value
        if (dot.number == state.currentTarget) {
            handleCorrectTap(dot)
        } else {
            handleWrongTap(dot)
        }
    }

    private fun handleCorrectTap(dot: DotTarget) {
        soundManager.playTapCorrect()
        hapticManager.vibrateCorrectTap()

        val updatedDots = _uiState.value.dots.map {
            if (it.id == dot.id) it.copy(completed = true) else it
        }

        val nextTarget = _uiState.value.currentTarget + 1
        val totalDots = _uiState.value.config.dotCount

        // Animate trail line
        animateTrail()

        if (nextTarget > totalDots) {
            // Player won!
            onGameWon(updatedDots)
        } else {
            _uiState.update {
                it.copy(
                    dots = updatedDots,
                    currentTarget = nextTarget,
                    lastCorrectDotId = dot.id
                )
            }
        }
    }

    private fun handleWrongTap(dot: DotTarget) {
        soundManager.playWrongTap()
        hapticManager.vibrateWrongTap()

        val penaltyMs = _uiState.value.config.wrongTapPenaltyMs
        accumulatedPenaltyMs += penaltyMs

        val limit = _uiState.value.timeLimitMs
        val now = SystemClock.elapsedRealtime()
        val currentPeriodElapsed = if (periodStartTimeRealtime > 0L) now - periodStartTimeRealtime else 0L
        val totalElapsed = currentPeriodElapsed + elapsedUnpausedMs + accumulatedPenaltyMs
        val remaining = max(0L, limit - totalElapsed)

        _uiState.update {
            it.copy(
                mistakes = it.mistakes + 1,
                lastWrongDotId = dot.id,
                wrongTapFeedbackTrigger = SystemClock.elapsedRealtime(),
                remainingTimeMs = remaining,
                showPenaltyPopup = true
            )
        }

        // Auto-dismiss penalty popup
        penaltyPopupJob?.cancel()
        penaltyPopupJob = viewModelScope.launch {
            delay(800L)
            _uiState.update { it.copy(showPenaltyPopup = false) }
        }

        if (remaining <= 0L) {
            onTimeExpired()
        }
    }

    private fun onGameWon(finalDots: List<DotTarget>) {
        timerJob?.cancel()
        val now = SystemClock.elapsedRealtime()
        val currentPeriodElapsed = if (periodStartTimeRealtime > 0L) now - periodStartTimeRealtime else 0L
        val completionTimeMs = currentPeriodElapsed + elapsedUnpausedMs
        val remainingTimeMs = max(0L, _uiState.value.timeLimitMs - (completionTimeMs + accumulatedPenaltyMs))

        soundManager.playWin()
        hapticManager.vibrateWin()

        val breakdown = ScoreCalculator.calculate(
            totalDots = _uiState.value.config.dotCount,
            remainingTimeMs = remainingTimeMs,
            mistakes = _uiState.value.mistakes,
            won = true
        )

        val (isNewScore, isNewTime) = preferencesManager.recordGameFinished(
            difficulty = _uiState.value.difficulty,
            level = _uiState.value.level,
            won = true,
            score = breakdown.finalScore,
            completionTimeMs = completionTimeMs,
            mistakes = _uiState.value.mistakes
        )

        val result = GameResult(
            level = _uiState.value.level,
            difficulty = _uiState.value.difficulty,
            totalDots = _uiState.value.config.dotCount,
            completedDots = _uiState.value.config.dotCount,
            completionTimeMs = completionTimeMs,
            timeLimitMs = _uiState.value.timeLimitMs,
            remainingTimeMs = remainingTimeMs,
            mistakes = _uiState.value.mistakes,
            score = breakdown.finalScore,
            won = true,
            isNewRecordScore = isNewScore,
            isNewRecordTime = isNewTime
        )

        _uiState.update {
            it.copy(
                gameState = GameState.WON,
                dots = finalDots,
                completionTimeMs = completionTimeMs,
                remainingTimeMs = remainingTimeMs,
                score = breakdown.finalScore,
                gameResult = result,
                isWarningTime = false
            )
        }
    }

    private fun onTimeExpired() {
        timerJob?.cancel()
        soundManager.playGameOver()
        hapticManager.vibrateGameOver()

        val completedCount = _uiState.value.dots.count { it.completed }
        val breakdown = ScoreCalculator.calculate(
            totalDots = _uiState.value.config.dotCount,
            remainingTimeMs = 0L,
            mistakes = _uiState.value.mistakes,
            won = false
        )

        preferencesManager.recordGameFinished(
            difficulty = _uiState.value.difficulty,
            level = _uiState.value.level,
            won = false,
            score = 0,
            completionTimeMs = null,
            mistakes = _uiState.value.mistakes
        )

        val result = GameResult(
            level = _uiState.value.level,
            difficulty = _uiState.value.difficulty,
            totalDots = _uiState.value.config.dotCount,
            completedDots = completedCount,
            completionTimeMs = null,
            timeLimitMs = _uiState.value.timeLimitMs,
            remainingTimeMs = 0L,
            mistakes = _uiState.value.mistakes,
            score = breakdown.finalScore,
            won = false
        )

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                remainingTimeMs = 0L,
                gameResult = result,
                isWarningTime = false
            )
        }
    }

    private fun animateTrail() {
        trailAnimJob?.cancel()
        trailAnimJob = viewModelScope.launch {
            val durationMs = 180L
            val steps = 12
            val stepDelay = durationMs / steps
            for (i in 1..steps) {
                _uiState.update { it.copy(trailAnimationProgress = i.toFloat() / steps) }
                delay(stepDelay)
            }
            _uiState.update { it.copy(trailAnimationProgress = 1f) }
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            timerJob?.cancel()
            val now = SystemClock.elapsedRealtime()
            if (periodStartTimeRealtime > 0L) {
                elapsedUnpausedMs += (now - periodStartTimeRealtime)
            }
            periodStartTimeRealtime = 0L
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            periodStartTimeRealtime = SystemClock.elapsedRealtime()
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startGameplayTimer()
        }
    }

    private fun cancelAllJobs() {
        timerJob?.cancel()
        countdownJob?.cancel()
        trailAnimJob?.cancel()
        penaltyPopupJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
