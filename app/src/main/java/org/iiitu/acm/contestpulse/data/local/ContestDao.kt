package org.iiitu.acm.contestpulse.data.local

import androidx.room.*
import org.iiitu.acm.contestpulse.data.model.Contest
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {

    @Query("SELECT * FROM contests ORDER BY startTimeMillis ASC")
    fun getAllContests(): Flow<List<Contest>>

    @Query("SELECT * FROM contests ORDER BY startTimeMillis ASC")
    suspend fun getAllContestsSync(): List<Contest>

    @Query("SELECT * FROM contests WHERE startTimeMillis >= :currentTime ORDER BY startTimeMillis ASC")
    fun getUpcomingContests(currentTime: Long): Flow<List<Contest>>

    @Query("SELECT * FROM contests WHERE startTimeMillis >= :currentTime ORDER BY startTimeMillis ASC")
    suspend fun getUpcomingContestsSync(currentTime: Long): List<Contest>

    @Query("SELECT * FROM contests WHERE startTimeMillis >= :startTime AND startTimeMillis <= :endTime ORDER BY startTimeMillis ASC")
    fun getWeeklyContests(startTime: Long, endTime: Long): Flow<List<Contest>>

    @Query("SELECT * FROM contests WHERE startTimeMillis >= :startTime AND startTimeMillis <= :endTime ORDER BY startTimeMillis ASC")
    suspend fun getWeeklyContestsSync(startTime: Long, endTime: Long): List<Contest>

    @Query("SELECT * FROM contests WHERE id = :id LIMIT 1")
    suspend fun getContestById(id: String): Contest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<Contest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContest(contest: Contest)

    @Query("UPDATE contests SET isAlarmEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAlarmStatus(id: String, isEnabled: Boolean)

    @Query("DELETE FROM contests WHERE id = :id")
    suspend fun deleteContest(id: String)

    @Query("DELETE FROM contests WHERE isCustom = 0 AND startTimeMillis < :cutoffTime")
    suspend fun deleteExpiredNonCustom(cutoffTime: Long)
}
