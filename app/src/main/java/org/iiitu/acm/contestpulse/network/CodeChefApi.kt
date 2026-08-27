package org.iiitu.acm.contestpulse.network

import org.iiitu.acm.contestpulse.data.model.Contest
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CodeChefApi(private val client: OkHttpClient) {

    fun fetchUpcomingContests(): List<Contest> {
        val contests = mutableListOf<Contest>()
        try {
            val request = Request.Builder()
                .url("https://www.codechef.com/api/list/contests/all")
                .header("User-Agent", "ContestPulse/1.0")
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: return emptyList()

            val jsonObject = JsonParser.parseString(bodyStr).asJsonObject
            if (jsonObject.has("future_contests")) {
                val arr = jsonObject.getAsJsonArray("future_contests")
                val now = System.currentTimeMillis()

                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

                for (elem in arr) {
                    val item = elem.asJsonObject
                    val code = item.get("contest_code").asString
                    val name = item.get("contest_name").asString

                    var startTimeMs = 0L
                    if (item.has("contest_start_date_iso")) {
                        try {
                            val dateStr = item.get("contest_start_date_iso").asString
                            val date = isoFormat.parse(dateStr)
                            if (date != null) {
                                startTimeMs = date.time
                            }
                        } catch (pe: Exception) {
                            pe.printStackTrace()
                        }
                    }

                    val durationMinutes = try {
                        item.get("contest_duration").asLong
                    } catch (e: Exception) {
                        120L
                    }

                    if (startTimeMs > now - 3600000) {
                        contests.add(
                            Contest(
                                id = "CC_" + code,
                                title = name,
                                platform = "CODECHEF",
                                startTimeMillis = startTimeMs,
                                durationMillis = durationMinutes * 60 * 1000L,
                                url = "https://www.codechef.com/$code",
                                isCustom = false,
                                isAlarmEnabled = true
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contests
    }
}
