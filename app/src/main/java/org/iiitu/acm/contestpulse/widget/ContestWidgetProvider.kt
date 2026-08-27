package org.iiitu.acm.contestpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import org.iiitu.acm.contestpulse.R
import org.iiitu.acm.contestpulse.data.local.ContestDatabase
import org.iiitu.acm.contestpulse.data.model.Contest
import org.iiitu.acm.contestpulse.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ContestWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ContestWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_contest_overview)

            // Intent to launch MainActivity when tapping widget header
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, mainPendingIntent)

            // Intent to trigger WidgetRefreshReceiver when clicking refresh button
            val refreshIntent = Intent(context, WidgetRefreshReceiver::class.java)
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 101, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnWidgetRefresh, refreshPendingIntent)

            // Load contests asynchronously from Room SQLite DB
            CoroutineScope(Dispatchers.IO).launch {
                val db = ContestDatabase.getDatabase(context)
                val now = System.currentTimeMillis()

                // Compute weekly bounds starting on Monday
                val cal = Calendar.getInstance(Locale.US).apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val weekEndMs = cal.timeInMillis - 1

                val upcoming = db.contestDao().getWeeklyContestsSync(now, weekEndMs)
                    .ifEmpty { db.contestDao().getUpcomingContestsSync(now) }

                val dayFormat = SimpleDateFormat("EEE hh:mm a", Locale.US)

                fun bindSlot(
                    platformId: Int,
                    titleId: Int,
                    timeId: Int,
                    contest: Contest?
                ) {
                    if (contest != null) {
                        views.setTextViewText(titleId, contest.title)

                        val diffMs = contest.startTimeMillis - now
                        val hours = diffMs / (1000 * 3600)
                        val days = hours / 24
                        val remHours = hours % 24
                        val relativeStr = if (days > 0) "${days}d ${remHours}h" else "${hours}h"

                        val timeStr = "${dayFormat.format(Date(contest.startTimeMillis))} (In $relativeStr)"
                        views.setTextViewText(timeId, timeStr)

                        when (contest.platform.uppercase()) {
                            "LEETCODE" -> {
                                views.setTextViewText(platformId, "LC")
                                views.setInt(platformId, "setBackgroundColor", Color.parseColor("#F59E0B"))
                            }
                            "CODECHEF" -> {
                                views.setTextViewText(platformId, "CC")
                                views.setInt(platformId, "setBackgroundColor", Color.parseColor("#8B5CF6"))
                            }
                            "CUSTOM" -> {
                                views.setTextViewText(platformId, "SET")
                                views.setInt(platformId, "setBackgroundColor", Color.parseColor("#06B6D4"))
                            }
                            else -> {
                                views.setTextViewText(platformId, "CF")
                                views.setInt(platformId, "setBackgroundColor", Color.parseColor("#EF4444"))
                            }
                        }
                    } else {
                        views.setTextViewText(titleId, "No contest scheduled")
                        views.setTextViewText(timeId, "Tap refresh to sync")
                        views.setTextViewText(platformId, "--")
                        views.setInt(platformId, "setBackgroundColor", Color.parseColor("#64748B"))
                    }
                }

                bindSlot(
                    R.id.tvPlatform1,
                    R.id.tvTitle1,
                    R.id.tvTime1,
                    upcoming.getOrNull(0)
                )

                bindSlot(
                    R.id.tvPlatform2,
                    R.id.tvTitle2,
                    R.id.tvTime2,
                    upcoming.getOrNull(1)
                )

                bindSlot(
                    R.id.tvPlatform3,
                    R.id.tvTitle3,
                    R.id.tvTime3,
                    upcoming.getOrNull(2)
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
