package dev.kiwik.tracking.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import dev.kiwik.tracking.domain.ApiUtils
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.toTracking
import dev.kiwik.tracking.utilities.isMobileDataEnabled
import dev.kiwik.tracking.utilities.isWifiEnabled
import java.util.concurrent.TimeUnit


class SyncTrackingWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val iRestService = ApiUtils.getCurrentRestService()
        val db = AppDatabase.getInstance()

        Log.v(TAG, "Syncronizando local tracking con servidor....")
        val trackingList = db.trackingBaseDao().getAll().chunked(5).first()

        if (trackingList.isEmpty()) {
            Log.v(TAG, "La lista esta vacia...")
            return Result.success()
        }


        Log.v(TAG, "Wifi activo: ${applicationContext.isWifiEnabled()}")
        Log.v(TAG, "Mobile activo: ${applicationContext.isMobileDataEnabled()}")

        val ids = trackingList.map { it.id!! }.toIntArray()
        trackingList.filter {
            it.latitude == 0.0 || it.longitude == 0.0
        }
        val response = iRestService.insertTracking(trackingList.map { it.toTracking() })
        if (!response.isSuccessful) return Result.failure()

        //db.trackingDao().deleteByIds(TrackingDaoUtil.deleteNotSyncQuery(ids.toTypedArray()))
        db.trackingBaseDao().deleteByIds(*ids)

        Log.v(TAG, "Posiciones enviada")
        return Result.success()
    }

    companion object {
        private const val TAG = "SyncTrackingWorker"

        @JvmStatic
        fun buildRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            return OneTimeWorkRequestBuilder<SyncTrackingWorker>()
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                    )
                    .setConstraints(constraints)
                    .build()
        }
    }
}