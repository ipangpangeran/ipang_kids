package com.smartkids.academy.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

object SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playCorrectSound() {
        try {
            // Cheerful 3-note victory melody (Do-Mi-Sol)
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_3, 120)
                } catch (_: Exception) {}
            }, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 250)
                } catch (_: Exception) {}
            }, 220)
        } catch (_: Exception) {}
    }

    fun playWrongSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 300)
        } catch (_: Exception) {}
    }

    fun playStarSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_8, 150)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 250)
                } catch (_: Exception) {}
            }, 150)
        } catch (_: Exception) {}
    }
}
