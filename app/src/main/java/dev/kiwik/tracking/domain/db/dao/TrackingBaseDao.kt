package dev.kiwik.tracking.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.kiwik.tracking.domain.entities.TrackingBase

@Dao
interface TrackingBaseDao {
    @Query("SELECT * FROM m_tracking_base")
    suspend fun getAll(): List<TrackingBase>

    @Insert
    suspend fun insert(vararg tracking: TrackingBase)

    @Query("DELETE FROM m_tracking_base")
    suspend fun truncate()

    @Query("DELETE FROM m_tracking_base WHERE id in (:ids)")
    suspend fun deleteByIds(vararg ids: Int)

    @Transaction
    suspend fun insertWith(vararg tracking: TrackingBase) = insert(*tracking)
}