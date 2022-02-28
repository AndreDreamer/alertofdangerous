package com.example.alertofdangerous

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.example.alertofdangerous.databinding.ActivityMainBinding
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import java.lang.Exception

class MainActivity : Activity() {

    private val appPreferences = "mysettings"
    private val appPreferencesActive = "Active"

    private lateinit var mSettings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupViews()
        setContentView(binding.root)
    }

    private fun setupViews() {

        checkActive()
        with(binding) {
            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    turnOnAlert()
                } else {
                    turnOffAlert()
                }
            }

            bDisableSignal.setOnClickListener {
                try {
                    AudioPlay.stopMusic()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkActive() {
        mSettings = getSharedPreferences(appPreferences, Context.MODE_PRIVATE)
        editor = mSettings.edit()

        val active = mSettings.getBoolean(appPreferencesActive, false)
        binding.toggleButton.isChecked = active
    }


    private fun turnOnAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOn,
            Toast.LENGTH_SHORT
        ).show()

        startForegroundService(Intent(this, ConnectionService::class.java))
        editor.putBoolean(appPreferencesActive, true)
        editor.apply()
    }

    private fun turnOffAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOff,
            Toast.LENGTH_SHORT
        ).show()

        stopService(Intent(this, ConnectionService::class.java))
        editor.putBoolean(appPreferencesActive, false)
        editor.apply()
    }

}

