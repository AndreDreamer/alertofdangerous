package com.middleview.alertofdangerous

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.middleview.alertofdangerous.databinding.FragmentMainBinding
import com.middleview.alertofdangerous.extensions.viewBinding

class MainFragment : Fragment() {

    private lateinit var service: ConnectionService
    private val binding by viewBinding(FragmentMainBinding::inflate)
    private var bound: Boolean = false
    private lateinit var connectivityManager: ConnectivityManager

    // Service connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as ConnectionService.LocalBinder).getService()
            bound = true
            try {
                binding.toggleButton.isChecked = service.running
                binder.addListener(object : ConnectionService.MyCallback {
                    override fun onCalled(text: String) {
                        activity?.runOnUiThread {
                            binding.textView.text = text
                        }
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupViews()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        bindConnectionService()
    }

    private fun bindConnectionService() {
        if (!bound) {
            val bindInt = Intent(activity, ConnectionService::class.java)
            bound = activity?.bindService(bindInt, connection, Activity.BIND_AUTO_CREATE) ?: false
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            activity?.unbindService(connection)
            bound = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupViews() {
        with(binding) {
            connectivityManager =
                activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    activity?.runOnUiThread {
                        progressBar.visibility = View.INVISIBLE
                        textViewInternetConn.visibility = View.INVISIBLE
                    }
                }

                override fun onLost(network: Network) {
                    activity?.runOnUiThread {
                        progressBar.visibility = View.VISIBLE
                        textViewInternetConn.visibility = View.VISIBLE
                    }
                }
            })


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
        activity?.startForegroundService(Intent(activity, ConnectionService::class.java))
        bindConnectionService()
    }


    private fun turnOffAlert() {
        showMessage(getString(R.string.infoSystemTurnedOff))
        try {
            activity?.unbindService(connection)
            bound = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        activity?.stopService(Intent(activity, ConnectionService::class.java))
    }

    private fun showMessage(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

}