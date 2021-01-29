package dev.kiwik.tracking.domain.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import dev.kiwik.tracking.domain.entities.Tracking

@Dao
interface TrackingDao {
    @Query("SELECT * FROM m_tracking order by createdAt desc")
    suspend fun getAll(): List<Tracking>

    @Query("SELECT * FROM m_tracking where userId = :id and strftime('%s',createdAt) BETWEEN strftime('%s',:initDate) And strftime('%s',:endDate)  ANd latitude != 0.0 AND  longitude != 0.0 order by id asc")
    fun getTracking(id: Int, initDate: String, endDate: String): LiveData<List<Tracking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg tracking: Tracking)

    @Query("DELETE FROM m_tracking")
    suspend fun truncate()

    @RawQuery
    fun deleteByIds(query: SupportSQLiteQuery): Long
}