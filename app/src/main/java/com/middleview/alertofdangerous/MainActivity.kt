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

    private lateinit var service: ConnectionService
    private lateinit var binding: ActivityMainBinding
    private var bound: Boolean = false

    // Service connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as ConnectionService.LocalBinder).getService()
            bound = true
            try {
                binding.toggleButton.isChecked = service.running
                binder.addListener(object : ConnectionService.MyCallback {
                    override fun onCalled(text: String) {
                        runOnUiThread { binding.textView.text = text }
                    }
                })
                if (service.waitingToStart) {
                    binding.textView.text = getString(R.string.tvInformationSafe)
                } else {
                    binding.textView.text = getString(R.string.tvInformationDangerous)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
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
        if (!bound) {
            val bindInt = Intent(this, ConnectionService::class.java)
            bound = bindService(bindInt, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unbindService(connection)
            bound = false
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
        startForegroundService(Intent(this, ConnectionService::class.java))
        bindConnectionService()
    }


    private fun turnOffAlert() {
        showMessage(getString(R.string.infoSystemTurnedOff))
        try {
            unbindService(connection)
            bound = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopService(Intent(this, ConnectionService::class.java))
    }

    private fun showMessage(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}

