package com.middleview.alertofdangerous

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit


class ConnectionService : Service() {

    private val baseURL = "https://t.me/s/dczloda/0"

    //    private val baseURL = "https://t.me/s/testtestuaforua/0"
    private var interval: Long = 30
    private val airAlarm: CharSequence = "Повітряна тривога"
    private val airAlarm2: CharSequence = "Усім укритися в сховищах"
    private val airAlarmCancel: CharSequence = "Відбій повітряної тривоги"


    private val binder = LocalBinder()
    var running = false
    var waitingToStart = true
    var waitingToStop = false
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
            someTask()
            createNotificationChannel()
            createNotification()
            wakeLockAction()
            AudioPlay.init(this)
        }
        return START_STICKY
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
            .setContentTitle(getString(R.string.app_name))
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

    private fun someTask() {
        running = true
        Thread {
            while (running) {
                try {
                    val document = Jsoup.connect(baseURL).get()
                    val link: Element =
                        document.select("div.tgme_widget_message_text").last()

                    if (waitingToStart && (link.toString().contains(airAlarm) || link.toString()
                            .contains(airAlarm2))
                    ) {
                        binder.mListener.onCalled(getString(R.string.tvInformationDangerous))
                        waitingToStop = true
                        waitingToStart = false
                        setMaxVolume()
                        AudioPlay.startMusic()
                    }

                    if (waitingToStop && link.toString().contains(airAlarmCancel)) {
                        binder.mListener.onCalled(getString(R.string.tvInformationSafe))
                        waitingToStop = false
                        waitingToStart = true
                        setMaxVolume()
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

    private fun setMaxVolume() {
        try {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



