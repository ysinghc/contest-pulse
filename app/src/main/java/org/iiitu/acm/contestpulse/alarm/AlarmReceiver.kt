package org.iiitu.acm.contestpulse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getStringExtra(AlarmScheduler.EXTRA_CONTEST_ID) ?: return
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_CONTEST_TITLE) ?: "Upcoming Contest"
        val platform = intent.getStringExtra(AlarmScheduler.EXTRA_CONTEST_PLATFORM) ?: "CODEFORCES"
        val startTime = intent.getLongExtra(AlarmScheduler.EXTRA_START_TIME, System.currentTimeMillis())

        // Start loud audio foreground service
        val serviceIntent = Intent(context, LoudAlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_CONTEST_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_CONTEST_PLATFORM, platform)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Start Full-Screen Alarm Activity
        val activityIntent = Intent(context, LoudAlarmActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_CONTEST_ID, contestId)
            putExtra(AlarmScheduler.EXTRA_CONTEST_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_CONTEST_PLATFORM, platform)
            putExtra(AlarmScheduler.EXTRA_START_TIME, startTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(activityIntent)
    }
}
