package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffects {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (_: Exception) {}
    }

    enum class SoundType {
        SUCCESS_COIN,
        DEPOSIT_SUBMIT,
        NOTIFICATION_PING,
        ALERT_WARNING,
        WITHDRAWAL_REQUEST
    }

    fun playSound(context: Context, type: SoundType, isSoundEnabled: Boolean) {
        if (!isSoundEnabled) return
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (type) {
                    SoundType.SUCCESS_COIN -> {
                        // Play delightful two-tone chime for coin reward
                        playSynthesizedChime(listOf(880, 1174, 1318), 90)
                    }
                    SoundType.DEPOSIT_SUBMIT -> {
                        playSynthesizedChime(listOf(587, 880), 80)
                    }
                    SoundType.NOTIFICATION_PING -> {
                        playSynthesizedChime(listOf(784, 1046), 100)
                    }
                    SoundType.ALERT_WARNING -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                    }
                    SoundType.WITHDRAWAL_REQUEST -> {
                        playSynthesizedChime(listOf(523, 659, 784, 1046), 80)
                    }
                }
            } catch (_: Exception) {
                // Fallback to ToneGenerator
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                } catch (_: Exception) {}
            }
        }
    }

    private fun playSynthesizedChime(frequencies: List<Int>, durationMs: Int) {
        try {
            val sampleRate = 44100
            val totalSamples = (frequencies.size * durationMs * sampleRate) / 1000
            val generatedSnd = ByteArray(2 * totalSamples)
            var sampleIndex = 0

            frequencies.forEach { freq ->
                val samplesForNote = (durationMs * sampleRate) / 1000
                for (i in 0 until samplesForNote) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq.toDouble())
                    val envelope = 1.0 - (i.toDouble() / samplesForNote) // Decay
                    val sample = (sin(angle) * 32767 * envelope * 0.7).toInt().toShort()
                    generatedSnd[sampleIndex++] = (sample.toInt() and 0xFF).toByte()
                    generatedSnd[sampleIndex++] = ((sample.toInt() shr 8) and 0xFF).toByte()
                }
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
        } catch (_: Exception) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        }
    }

    fun vibrate(context: Context, isVibrationEnabled: Boolean = true) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {}
    }
}
