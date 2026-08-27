package org.iiitu.acm.contestpulse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.iiitu.acm.contestpulse.data.repository.ContestRepository
import org.iiitu.acm.contestpulse.worker.ContestSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Re-schedule weekly WorkManager sync
            ContestSyncWorker.scheduleWeeklySync(context)

            // Reschedule exact 15-min alarms for stored contests
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = ContestRepository(context)
                    repo.refreshContests()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
