package org.iiitu.acm.contestpulse.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.iiitu.acm.contestpulse.data.model.Contest

object AlarmScheduler {

    const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    const val EXTRA_CONTEST_ID = "extra_contest_id"
    const val EXTRA_CONTEST_TITLE = "extra_contest_title"
    const val EXTRA_CONTEST_PLATFORM = "extra_contest_platform"
    const val EXTRA_START_TIME = "extra_start_time"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, contest: Contest) {
        val alarmTimeMs = contest.startTimeMillis - FIFTEEN_MINUTES_MS
        val now = System.currentTimeMillis()

        if (alarmTimeMs <= now) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_CONTEST_ID, contest.id)
            putExtra(EXTRA_CONTEST_TITLE, contest.title)
            putExtra(EXTRA_CONTEST_PLATFORM, contest.platform)
            putExtra(EXTRA_START_TIME, contest.startTimeMillis)
        }

        val requestCode = contest.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMs,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, contestId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            contestId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun triggerTestAlarm(context: Context) {
        val intent = Intent(context, LoudAlarmActivity::class.java).apply {
            putExtra(EXTRA_CONTEST_ID, "TEST_ALARM")
            putExtra(EXTRA_CONTEST_TITLE, "Demo 15-Minute Loud Ringing Alarm")
            putExtra(EXTRA_CONTEST_PLATFORM, "LEETCODE")
            putExtra(EXTRA_START_TIME, System.currentTimeMillis() + FIFTEEN_MINUTES_MS)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val serviceIntent = Intent(context, LoudAlarmService::class.java).apply {
            putExtra(EXTRA_CONTEST_TITLE, "Demo 15-Minute Loud Ringing Alarm")
            putExtra(EXTRA_CONTEST_PLATFORM, "LEETCODE")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        context.startActivity(intent)
    }
}
