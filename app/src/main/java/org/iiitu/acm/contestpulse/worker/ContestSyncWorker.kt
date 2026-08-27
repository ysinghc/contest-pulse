package org.iiitu.acm.contestpulse.worker

import android.content.Context
import androidx.work.*
import org.iiitu.acm.contestpulse.data.repository.ContestRepository
import java.util.concurrent.TimeUnit

class ContestSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = ContestRepository(applicationContext)
        val result = repository.refreshContests()
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME_PERIODIC = "ContestWeeklySyncWorker"
        private const val WORK_NAME_ONE_TIME = "ContestImmediateSyncWorker"

        fun scheduleWeeklySync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ContestSyncWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ContestSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
