package com.example.alertofdangerous

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.example.alertofdangerous.databinding.ActivityMainBinding
import android.content.Intent
import android.content.SharedPreferences

class MainActivity : Activity() {

    private val APP_PREFERENCES = "mysettings"
    private val APP_PREFERENCES_ACTIVE = "Active"


    private lateinit var mSettings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AudioPlay.init(applicationContext)
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
                AudioPlay.stopMusic()
            }
        }
    }

    private fun checkActive() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        editor = mSettings.edit()

        val active = mSettings.getBoolean(APP_PREFERENCES_ACTIVE, false)
        binding.toggleButton.isChecked = active
    }


    private fun turnOnAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOn,
            Toast.LENGTH_SHORT
        ).show()

        startForegroundService(Intent(this, ConnectionService::class.java))
        editor.putBoolean(APP_PREFERENCES_ACTIVE, true)
        editor.apply()
    }

    private fun turnOffAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOff,
            Toast.LENGTH_SHORT
        ).show()

        stopService(Intent(this, ConnectionService::class.java))
        AudioPlay.stopMusic()
        editor.putBoolean(APP_PREFERENCES_ACTIVE, false)
        editor.apply()
    }

}

