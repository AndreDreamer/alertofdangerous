package com.middleview.alertofdangerous

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class ConnectionService : Service() {

    private val baseURL = "https://t.me/s/dczloda/0"

    //    private val baseURL = "https://t.me/s/testtestuaforua/0"
    private var interval: Long = 30
    private val airAlarm: CharSequence = "повітряна тривога"
    private val airAlarm2: CharSequence = "укритися"
    private val airAlarmCancel: CharSequence = "відбій"


    private val binder = LocalBinder()
    var running = false
    var waitingToStart = true
    private val channelID = "alert program notification"
    private lateinit var wakeLock: PowerManager.WakeLock

    inner class LocalBinder : Binder() {
        lateinit var mListener: MyCallback

        fun getService(): ConnectionService = this@ConnectionService
        fun addListener(listener: MyCallback) {
            mListener = listener
        }
    }

    interface MyCallback {
        fun onCalled(text: String)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            createNotificationChannel()
            createNotification()
            checkForIgnoreBatteryOptimization()
            wakeLockAction()
            scheduledTask()
            AudioPlay.init(this)
        }
        return START_STICKY
    }

    private fun checkForIgnoreBatteryOptimization() {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val packageName: String = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager?
            if (!pm!!.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun wakeLockAction() {
        try {
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlertOfDangerous::lock").apply {
                        acquire()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelID,
            "Channel of alert program notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

    }

    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, channelID)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentText(getString(R.string.infoSystemTurnedOn))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        running = false
        AudioPlay.stopMusic()
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun scheduledTask() {
        running = true
        Thread {
            while (running) {
                try {
                    val document = Jsoup.connect(baseURL).get()
                    val link: Element =
                        document.select("div.tgme_widget_message_text").last()
                    val text = link.toString().lowercase()

                    // Check for dangerous
                    if (waitingToStart && (text.contains(airAlarm) || text.contains(airAlarm2))) {
                        binder.mListener.onCalled(getString(R.string.tvInformationDangerous))
                        waitingToStart = false
                        AudioPlay.setMaxVolume(this)
                        AudioPlay.startMusic()
                    }

                    // Check for safe
                    if (!waitingToStart && text.contains(airAlarmCancel)) {
                        binder.mListener.onCalled(getString(R.string.tvInformationSafe))
                        waitingToStart = true
                        AudioPlay.setMaxVolume(this)
                        AudioPlay.startMusic()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    TimeUnit.SECONDS.sleep(interval)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}



