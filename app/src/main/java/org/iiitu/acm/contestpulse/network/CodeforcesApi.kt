package org.iiitu.acm.contestpulse.network

import org.iiitu.acm.contestpulse.data.model.Contest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CodeforcesApi(private val client: OkHttpClient) {

    fun fetchUpcomingContests(): List<Contest> {
        val contests = mutableListOf<Contest>()
        try {
            val request = Request.Builder()
                .url("https://codeforces.com/api/contest.list?gym=false")
                .header("User-Agent", "ContestPulse/1.0")
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: return emptyList()

            val jsonObject = JsonParser.parseString(bodyStr).asJsonObject
            if (jsonObject.has("status") && jsonObject.get("status").asString == "OK") {
                val resultArr = jsonObject.getAsJsonArray("result")
                val now = System.currentTimeMillis()

                for (elem in resultArr) {
                    val item = elem.asJsonObject
                    val phase = item.get("phase").asString
                    if (phase == "BEFORE") {
                        val id = "CF_" + item.get("id").asLong
                        val name = item.get("name").asString
                        val startTimeSec = item.get("startTimeSeconds").asLong
                        val durationSec = item.get("durationSeconds").asLong
                        val startTimeMs = startTimeSec * 1000L
                        val durationMs = durationSec * 1000L

                        if (startTimeMs > now - 3600000) {
                            contests.add(
                                Contest(
                                    id = id,
                                    title = name,
                                    platform = "CODEFORCES",
                                    startTimeMillis = startTimeMs,
                                    durationMillis = durationMs,
                                    url = "https://codeforces.com/contests/" + item.get("id").asLong,
                                    isCustom = false,
                                    isAlarmEnabled = true
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contests
    }
}
