package com.example.alertofdangerous

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class ConnectionService : Service() {

    private val baseURL = "https://t.me/s/andriysadovyi/0"
    private var interval: Long = 30
    private val airAlarm: CharSequence = "Повітряна тривога"
    private val airAlarmCancel: CharSequence = "Відбій повітряної тривоги"

    private var running = false
    private var waitingToStart = true
    private var waitingToStop = false
    private val channelID = "alert program notification"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        someTask()
        notification()
        return START_STICKY
    }

    private fun notification() {
        val channel = NotificationChannel(
            channelID,
            "Channel of alert program notification",
            NotificationManager.IMPORTANCE_HIGH
        )

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


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
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun someTask() {
        running = true
        Thread {
            while (running) {
                try {
                    val document = Jsoup.connect(baseURL).get()
                    val link: Element =
                        document.select("div.tgme_widget_message_text").last()

                    if (waitingToStart && link.toString().contains(airAlarm)) {
                        waitingToStop = true
                        waitingToStart = false
                        AudioPlay.startMusic()
                    }

                    if (waitingToStop && link.toString().contains(airAlarmCancel)) {
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

