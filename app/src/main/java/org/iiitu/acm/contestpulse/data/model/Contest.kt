package org.iiitu.acm.contestpulse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contests")
data class Contest(
    @PrimaryKey
    val id: String,
    val title: String,
    val platform: String, // "CODEFORCES", "LEETCODE", "CODECHEF", "CUSTOM"
    val startTimeMillis: Long,
    val durationMillis: Long,
    val url: String = "",
    val isCustom: Boolean = false,
    val isAlarmEnabled: Boolean = true,
    val dayOfWeek: String = "", // e.g. "SUNDAY", "WEDNESDAY"
    val timeString: String = "", // e.g. "08:00 PM"
    val updatedAtMillis: Long = System.currentTimeMillis()
)
