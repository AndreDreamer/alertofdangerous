package com.middleview.alertofdangerous

import android.content.Context
import android.media.MediaPlayer
import java.lang.Exception


object AudioPlay {
    private lateinit var mediaPlayer: MediaPlayer

    fun init(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.sound_alert)
        mediaPlayer.isLooping = true
    }

    fun startMusic() {
        mediaPlayer.start()
    }

    fun stopMusic() {
        try {
            mediaPlayer.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}