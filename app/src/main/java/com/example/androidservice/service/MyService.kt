package com.example.androidservice.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.androidservice.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MyService : Service() {

    private var isProcessRunning: Boolean = false

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MyChannel"
        private const val ACTION_STOP = "com.example.app.STOP_SERVICE"
        var currentTimer = MutableLiveData(0)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP) {
            Log.i("TAG", "onStartCommand: Notification stopping service")
            stopSelf()
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        runProcess()
    }

    private fun runProcess() {
        isProcessRunning = true
        CoroutineScope(Dispatchers.IO).launch {
            while (isProcessRunning) {
                var count = currentTimer.value ?: 0
                count++
                currentTimer.postValue(count)
                Log.i("TAG", "runProcess: ${currentTimer.value}")
                delay(2.seconds)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopServiceIntent = Intent(this, MyService::class.java)
        stopServiceIntent.action = ACTION_STOP
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the open app intent
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        // Build the notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("My App")
            .setContentText("App is running")
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.stop_icon, "Stop", stopServicePendingIntent)

        return builder.build()
    }

    private fun openApp() {
        val packageName = packageName // Get the package name of your app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }


    override fun onDestroy() {
        isProcessRunning = false
        currentTimer.postValue(0)
        super.onDestroy()

    }
}