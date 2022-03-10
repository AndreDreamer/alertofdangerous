package com.middleview.alertofdangerous

import android.app.Service
import android.content.Context
import android.media.AudioManager
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
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }
    }

    fun setMaxVolume(service: ConnectionService) {
        val audioManager = service.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }
}