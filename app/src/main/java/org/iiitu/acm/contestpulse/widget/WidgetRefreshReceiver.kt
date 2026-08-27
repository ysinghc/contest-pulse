package org.iiitu.acm.contestpulse.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.iiitu.acm.contestpulse.worker.ContestSyncWorker

class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Syncing contests from Widget...", Toast.LENGTH_SHORT).show()
        ContestSyncWorker.enqueueImmediateSync(context)
    }
}
