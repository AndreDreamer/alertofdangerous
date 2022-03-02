package com.middleview.alertofdangerous

import android.content.Context
import android.media.MediaPlayer


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
        mediaPlayer.pause()

    }
}