package com.middleview.alertofdangerous

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.middleview.alertofdangerous.databinding.ActivityMainBinding


class MainActivity : Activity() {

    private lateinit var mService: ConnectionService
    private var mBound: Boolean = false

    private lateinit var binding: ActivityMainBinding

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = (service as ConnectionService.LocalBinder).getService()
            mBound = true
            try {
                binding.toggleButton.isChecked = mService.running

                service.addListener(object : ConnectionService.MyCallback {
                    override fun onCalled(text: String) {
                        runOnUiThread { binding.textView.text = text }

                    }
                })
                if (mService.waitingToStart) {
                    binding.textView.text = getString(R.string.tvInformationSafe)
                } else {
                    binding.textView.text = getString(R.string.tvInformationDangerous)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupViews()
        setContentView(binding.root)
    }


    override fun onResume() {
        super.onResume()
        bindConnectionService()
    }

    private fun bindConnectionService() {
        if (!mBound) {
            val bindInt = Intent(this, ConnectionService::class.java)
            mBound = bindService(bindInt, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unbindService(connection)
            mBound = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupViews() {
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


    private fun turnOnAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOn,
            Toast.LENGTH_SHORT
        ).show()

        startForegroundService(Intent(this, ConnectionService::class.java))
        bindConnectionService()
    }


    private fun turnOffAlert() {
        Toast.makeText(
            applicationContext,
            R.string.infoSystemTurnedOff,
            Toast.LENGTH_SHORT
        ).show()

        try {
            unbindService(connection)
            mBound = false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stopService(Intent(this, ConnectionService::class.java))
    }
}

