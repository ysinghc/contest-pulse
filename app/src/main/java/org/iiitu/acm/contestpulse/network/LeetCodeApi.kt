package org.iiitu.acm.contestpulse.network

import org.iiitu.acm.contestpulse.data.model.Contest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LeetCodeApi(private val client: OkHttpClient) {

    fun fetchUpcomingContests(): List<Contest> {
        val contests = mutableListOf<Contest>()
        try {
            val queryJson = """
                {"query":"query { topTwoContests { title titleSlug startTime duration } }"}
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = queryJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://leetcode.com/graphql")
                .post(body)
                .header("User-Agent", "ContestPulse/1.0")
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: return emptyList()

            val jsonObject = JsonParser.parseString(bodyStr).asJsonObject
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull) {
                val dataObj = jsonObject.getAsJsonObject("data")
                if (dataObj.has("topTwoContests") && !dataObj.get("topTwoContests").isJsonNull) {
                    val arr = dataObj.getAsJsonArray("topTwoContests")
                    val now = System.currentTimeMillis()

                    for (elem in arr) {
                        val item = elem.asJsonObject
                        val title = item.get("title").asString
                        val titleSlug = item.get("titleSlug").asString
                        val startTimeSec = item.get("startTime").asLong
                        val durationSec = item.get("duration").asLong
                        val startTimeMs = startTimeSec * 1000L
                        val durationMs = durationSec * 1000L

                        if (startTimeMs > now - 3600000) {
                            contests.add(
                                Contest(
                                    id = "LC_" + titleSlug,
                                    title = title,
                                    platform = "LEETCODE",
                                    startTimeMillis = startTimeMs,
                                    durationMillis = durationMs,
                                    url = "https://leetcode.com/contest/$titleSlug",
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
