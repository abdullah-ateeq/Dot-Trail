package com.example.dottrail.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffectManager {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    var isEnabled: Boolean = true

    // Pre-generated PCM audio buffers for instant low-latency playback
    private val tapCorrectBuffer by lazy { generateTone(880f, 1175f, 0.08f) }
    private val wrongTapBuffer by lazy { generateBuzzer(160f, 0.12f) }
    private val countdownTickBuffer by lazy { generateTone(600f, 600f, 0.05f) }
    private val countdownGoBuffer by lazy { generateTone(1046f, 1318f, 0.18f) }
    private val winBuffer by lazy { generateArpeggio(listOf(523f, 659f, 784f, 1046f), 0.09f) }
    private val gameOverBuffer by lazy { generateArpeggio(listOf(440f, 370f, 293f), 0.12f) }

    fun playTapCorrect() {
        if (!isEnabled) return
        playPcm(tapCorrectBuffer)
    }

    fun playWrongTap() {
        if (!isEnabled) return
        playPcm(wrongTapBuffer)
    }

    fun playCountdownTick() {
        if (!isEnabled) return
        playPcm(countdownTickBuffer)
    }

    fun playCountdownGo() {
        if (!isEnabled) return
        playPcm(countdownGoBuffer)
    }

    fun playWin() {
        if (!isEnabled) return
        playPcm(winBuffer)
    }

    fun playGameOver() {
        if (!isEnabled) return
        playPcm(gameOverBuffer)
    }

    private fun playPcm(buffer: ShortArray) {
        audioScope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Clean up track after duration
                val durationMs = (buffer.size * 1000L) / SAMPLE_RATE
                kotlinx.coroutines.delay(durationMs + 50L)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Audio synthesis error fallback without crashing
            }
        }
    }

    companion object {
        private const val SAMPLE_RATE = 22050

        private fun generateTone(startFreq: Float, endFreq: Float, durationSec: Float): ShortArray {
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(totalSamples)
            var phase = 0.0

            for (i in 0 until totalSamples) {
                val progress = i.toFloat() / totalSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
                val envelope = when {
                    progress < 0.1f -> progress / 0.1f
                    progress > 0.8f -> (1f - progress) / 0.2f
                    else -> 1f
                }
                val sampleValue = (sin(phase) * envelope * 0.7 * Short.MAX_VALUE).toInt()
                samples[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            return samples
        }

        private fun generateBuzzer(freq: Float, durationSec: Float): ShortArray {
            val totalSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(totalSamples)
            var phase = 0.0

            for (i in 0 until totalSamples) {
                val progress = i.toFloat() / totalSamples
                phase += 2.0 * Math.PI * freq / SAMPLE_RATE
                val square = if (sin(phase) >= 0) 1.0 else -1.0
                val envelope = (1f - progress) // fade out
                val sampleValue = (square * envelope * 0.5 * Short.MAX_VALUE).toInt()
                samples[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            return samples
        }

        private fun generateArpeggio(frequencies: List<Float>, noteDurationSec: Float): ShortArray {
            val noteSamples = (SAMPLE_RATE * noteDurationSec).toInt()
            val totalSamples = noteSamples * frequencies.size
            val samples = ShortArray(totalSamples)

            for (n in frequencies.indices) {
                val freq = frequencies[n]
                val offset = n * noteSamples
                var phase = 0.0

                for (i in 0 until noteSamples) {
                    phase += 2.0 * Math.PI * freq / SAMPLE_RATE
                    val progress = i.toFloat() / noteSamples
                    val envelope = when {
                        progress < 0.1f -> progress / 0.1f
                        else -> 1f - (progress * 0.7f)
                    }
                    val sampleValue = (sin(phase) * envelope * 0.65 * Short.MAX_VALUE).toInt()
                    samples[offset + i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            return samples
        }
    }
}
