package dev.kiwik.tracking.repository

import dev.kiwik.tracking.domain.api.*
import dev.kiwik.tracking.domain.db.dao.TrackingDao
import dev.kiwik.tracking.preferences.Pref
import retrofit2.Response

class TrackingRepository(private val dao: TrackingDao) {

    private val pref by lazy {
        Pref.getInstance()
    }

    fun getTracking(initDate: String, endDate: String) = dao.getTracking(pref.values.loggedUser?.id ?: 0, initDate, endDate)


    suspend fun getTrackingFromDb(request : GetTrackingRequest) : Response<GetTrackingResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        val method = iRestService::getTracking
        return method.invoke(request)
    }

    companion object {

        @Volatile
        private var instance: TrackingRepository? = null

        fun getInstance(dao: TrackingDao) =
            instance ?: synchronized(this) {
                instance ?: TrackingRepository(dao).also { instance = it }
            }
    }
}