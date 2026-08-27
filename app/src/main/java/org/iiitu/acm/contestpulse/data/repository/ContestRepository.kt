package org.iiitu.acm.contestpulse.data.repository

import android.content.Context
import org.iiitu.acm.contestpulse.alarm.AlarmScheduler
import org.iiitu.acm.contestpulse.data.local.ContestDao
import org.iiitu.acm.contestpulse.data.local.ContestDatabase
import org.iiitu.acm.contestpulse.data.model.Contest
import org.iiitu.acm.contestpulse.network.CodeChefApi
import org.iiitu.acm.contestpulse.network.CodeforcesApi
import org.iiitu.acm.contestpulse.network.LeetCodeApi
import org.iiitu.acm.contestpulse.widget.ContestWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ContestRepository(private val context: Context) {

    private val contestDao: ContestDao = ContestDatabase.getDatabase(context).contestDao()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val cfApi = CodeforcesApi(client)
    private val lcApi = LeetCodeApi(client)
    private val ccApi = CodeChefApi(client)

    fun getAllContests(): Flow<List<Contest>> = contestDao.getAllContests()

    fun getUpcomingContests(currentTime: Long = System.currentTimeMillis()): Flow<List<Contest>> =
        contestDao.getUpcomingContests(currentTime)

    fun getWeeklyContests(startTime: Long, endTime: Long): Flow<List<Contest>> =
        contestDao.getWeeklyContests(startTime, endTime)

    suspend fun addCustomContest(contest: Contest) {
        withContext(Dispatchers.IO) {
            contestDao.insertContest(contest)
            // Schedule alarm if enabled
            if (contest.isAlarmEnabled && contest.startTimeMillis > System.currentTimeMillis()) {
                AlarmScheduler.scheduleAlarm(context, contest)
            }
            ContestWidgetProvider.updateAllWidgets(context)
        }
    }

    suspend fun toggleAlarm(contestId: String, isEnabled: Boolean) {
        withContext(Dispatchers.IO) {
            contestDao.updateAlarmStatus(contestId, isEnabled)
            val contest = contestDao.getContestById(contestId)
            if (contest != null) {
                if (isEnabled && contest.startTimeMillis > System.currentTimeMillis()) {
                    AlarmScheduler.scheduleAlarm(context, contest)
                } else {
                    AlarmScheduler.cancelAlarm(context, contest.id)
                }
            }
            ContestWidgetProvider.updateAllWidgets(context)
        }
    }

    suspend fun deleteContest(contestId: String) {
        withContext(Dispatchers.IO) {
            AlarmScheduler.cancelAlarm(context, contestId)
            contestDao.deleteContest(contestId)
            ContestWidgetProvider.updateAllWidgets(context)
        }
    }

    suspend fun refreshContests(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val allFetched = mutableListOf<Contest>()

                // Fetch from Codeforces
                val cfList = cfApi.fetchUpcomingContests()
                allFetched.addAll(cfList)

                // Fetch from LeetCode
                val lcList = lcApi.fetchUpcomingContests()
                allFetched.addAll(lcList)

                // Fetch from CodeChef
                val ccList = ccApi.fetchUpcomingContests()
                allFetched.addAll(ccList)

                val now = System.currentTimeMillis()

                // If online fetch returned results, store in SQLite DB
                if (allFetched.isNotEmpty()) {
                    contestDao.deleteExpiredNonCustom(now - 7 * 24 * 3600 * 1000L)
                    contestDao.insertContests(allFetched)
                } else {
                    // Fallback generator for predictable recurring weekly contest slots if offline/first run
                    ensureFallbackWeeklySchedules()
                }

                // Reschedule 15-min alarms for all enabled upcoming contests
                val upcoming = contestDao.getUpcomingContestsSync(now)
                for (contest in upcoming) {
                    if (contest.isAlarmEnabled && contest.startTimeMillis > now) {
                        AlarmScheduler.scheduleAlarm(context, contest)
                    }
                }

                // Update Homescreen Widget
                ContestWidgetProvider.updateAllWidgets(context)

                Result.success(upcoming.size)
            } catch (e: Exception) {
                e.printStackTrace()
                // Ensure fallback schedules in case of complete network failure
                ensureFallbackWeeklySchedules()
                Result.failure(e)
            }
        }
    }

    private suspend fun ensureFallbackWeeklySchedules() {
        val now = System.currentTimeMillis()
        val existing = contestDao.getUpcomingContestsSync(now)
        if (existing.isEmpty()) {
            val fallbackContests = generateDefaultWeeklyContests()
            contestDao.insertContests(fallbackContests)
        }
    }

    private fun generateDefaultWeeklyContests(): List<Contest> {
        val list = mutableListOf<Contest>()
        val cal = Calendar.getInstance()

        // LeetCode Weekly Contest - Every Sunday 08:00 AM
        val lcWeeklyCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        list.add(
            Contest(
                id = "LC_WEEKLY_" + lcWeeklyCal.timeInMillis,
                title = "LeetCode Weekly Contest",
                platform = "LEETCODE",
                startTimeMillis = lcWeeklyCal.timeInMillis,
                durationMillis = 90 * 60 * 1000L,
                url = "https://leetcode.com/contest/",
                dayOfWeek = "SUNDAY",
                timeString = "08:00 AM"
            )
        )

        // CodeChef Starters - Every Wednesday 08:00 PM
        val ccStartersCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        list.add(
            Contest(
                id = "CC_STARTERS_" + ccStartersCal.timeInMillis,
                title = "CodeChef Starters",
                platform = "CODECHEF",
                startTimeMillis = ccStartersCal.timeInMillis,
                durationMillis = 120 * 60 * 1000L,
                url = "https://www.codechef.com/",
                dayOfWeek = "WEDNESDAY",
                timeString = "08:00 PM"
            )
        )

        // Codeforces Round (Div. 2) - Every Saturday 08:05 PM
        val cfDiv2Cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        list.add(
            Contest(
                id = "CF_ROUND_" + cfDiv2Cal.timeInMillis,
                title = "Codeforces Round (Div. 2)",
                platform = "CODEFORCES",
                startTimeMillis = cfDiv2Cal.timeInMillis,
                durationMillis = 120 * 60 * 1000L,
                url = "https://codeforces.com/contests",
                dayOfWeek = "SATURDAY",
                timeString = "08:05 PM"
            )
        )

        return list
    }
}
