package com.example.alertofdangerous

import android.content.Context
import android.media.MediaPlayer

object AudioPlay {
    private lateinit var mediaPlayer: MediaPlayer

    fun init(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.sound_alert)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        mediaPlayer.pause()
    }

    fun startMusic() {
        mediaPlayer.start()
    }

    fun stopMusic() {
        mediaPlayer.pause()

    }
}