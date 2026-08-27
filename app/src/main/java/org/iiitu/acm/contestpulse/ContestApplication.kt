package org.iiitu.acm.contestpulse

import android.app.Application
import org.iiitu.acm.contestpulse.worker.ContestSyncWorker

class ContestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Schedule periodic weekly sync
        ContestSyncWorker.scheduleWeeklySync(this)
    }
}
