package dev.kiwik.tracking.domain.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.domain.entities.Place
import dev.kiwik.tracking.domain.entities.Tracking

@Dao
interface ResourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(vararg category: Category)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlace(vararg place: Place)

    @Query("Select * from m_category")
    suspend fun getAllCategories(): List<Category>

    @Query("Select * from m_place where place_id = :id")
    suspend fun getPlace(id: String): Place

    @Query("Select * from m_place order by id desc")
    fun getPlaces(): LiveData<List<Place>>

    @Query("DELETE FROM m_category")
    suspend fun truncateCategories()

    @Query("DELETE FROM m_place")
    suspend fun truncatePlaces()

}