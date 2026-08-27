package org.iiitu.acm.contestpulse.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import org.iiitu.acm.contestpulse.R

class LoudAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "contest_alarm_channel"
        const val NOTIFICATION_ID = 9991
        const val ACTION_STOP_ALARM = "org.iiitu.acm.contestpulse.ACTION_STOP_ALARM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val title = intent?.getStringExtra(AlarmScheduler.EXTRA_CONTEST_TITLE) ?: "Contest Alarm"
        val platform = intent?.getStringExtra(AlarmScheduler.EXTRA_CONTEST_PLATFORM) ?: "CODEFORCES"

        val notification = buildForegroundNotification(title, platform)
        startForeground(NOTIFICATION_ID, notification)

        startLoudRingtoneAndVibration()

        return START_STICKY
    }

    private fun startLoudRingtoneAndVibration() {
        try {
            // Set max volume for alarm stream
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            // Ringtone / MediaPlayer setup
            var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer?.stop()
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start high-intensity emergency vibration
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 250, 500, 250, 500, 250)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtoneAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Contest 15-Min Loud Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency loud alarms 15 minutes before coding contests start"
                setSound(null, null)
                enableVibration(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(title: String, platform: String): Notification {
        val stopIntent = Intent(this, LoudAlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Contest Starting in 15 Minutes!")
            .setContentText("$title ($platform)")
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_stop, "Stop Alarm", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopRingtoneAndVibration()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
