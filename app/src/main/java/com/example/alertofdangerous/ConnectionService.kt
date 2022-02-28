package com.example.alertofdangerous

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ConnectionService : Service() {

    private val BASE_URL = "https://t.me/s/andriysadovyi/0"
    private var interval: Long = 30
    private val AIR_ALARM: CharSequence = "Повітряна тривога"
    private val AIR_ALARM_CANCEL: CharSequence = "Відбій повітряної тривоги"

    private val LOG_TAG = "myLogs"
    private var running = false

    private var waitingToStart = true
    private var waitingToStop = false
    private val CHANNEL_ID = "alert program notification"

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand")
        someTask()
        notification()
        return START_STICKY
    }

    private fun notification() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel of alert program notification",
            NotificationManager.IMPORTANCE_HIGH
        )

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.infoSystemTurnedOn)).build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")
        running = false
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LOG_TAG, "onBind")
        return null
    }

    private fun someTask() {
        running = true
        Thread {
            while (running) {
                try {
                    val document = Jsoup.connect(BASE_URL).get()
                    val link: Element =
                        document.select("div.tgme_widget_message_text").last()

                    if (waitingToStart && link.toString().contains(AIR_ALARM)) {
                        waitingToStop = true
                        waitingToStart = false
                        AudioPlay.startMusic()
                    }

                    if (waitingToStop && link.toString().contains(AIR_ALARM_CANCEL)) {
                        waitingToStop = false
                        waitingToStart = true
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

