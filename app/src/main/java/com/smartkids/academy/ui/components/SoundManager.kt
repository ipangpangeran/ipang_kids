package com.smartkids.academy.ui.components

import android.content.Context
import android.media.MediaPlayer
import com.smartkids.academy.R

object SoundManager {
    private var bgPlayer: MediaPlayer? = null

    fun startBackgroundMusic(context: Context) {
        try {
            if (bgPlayer == null) {
                bgPlayer = MediaPlayer.create(context.applicationContext, R.raw.backsound).apply {
                    isLooping = true
                    setVolume(0.35f, 0.35f)
                }
            }
            if (bgPlayer?.isPlaying == false) {
                bgPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    fun pauseBackgroundMusic() {
        try {
            if (bgPlayer?.isPlaying == true) {
                bgPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeBackgroundMusic() {
        try {
            if (bgPlayer != null && bgPlayer?.isPlaying == false) {
                bgPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    fun stopBackgroundMusic() {
        try {
            bgPlayer?.stop()
            bgPlayer?.release()
            bgPlayer = null
        } catch (_: Exception) {}
    }

    fun playCorrectSound(context: Context) {
        try {
            val mp = MediaPlayer.create(context.applicationContext, R.raw.correct)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (_: Exception) {}
    }

    fun playWrongSound(context: Context) {
        try {
            val mp = MediaPlayer.create(context.applicationContext, R.raw.incorrect)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (_: Exception) {}
    }

    fun playStarSound(context: Context? = null) {
        try {
            context?.let {
                val mp = MediaPlayer.create(it.applicationContext, R.raw.correct)
                mp?.setOnCompletionListener { p -> p.release() }
                mp?.start()
            }
        } catch (_: Exception) {}
    }
}
