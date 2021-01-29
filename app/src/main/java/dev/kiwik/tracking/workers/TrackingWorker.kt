package dev.kiwik.tracking.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import dev.kiwik.tracking.domain.api.GetTrackingRequest
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.repository.TrackingRepository
import dev.kiwik.tracking.utilities.ExceptionUtil
import dev.kiwik.tracking.utilities.isNull
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class TrackingWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.v(SyncTokenWorker.TAG, "---- ${SyncTokenWorker.TAG} ----")
        return try {
            return runBlocking {
                val pref = Pref.getInstance()
                val db = AppDatabase.getInstance()
                val trackingRepository = TrackingRepository.getInstance(db.trackingDao())
                val filter = pref.values.filterParamTracking
                val result = trackingRepository.getTrackingFromDb(GetTrackingRequest(pref.values.loggedUser?.id
                        ?: 0, filter.initDate, filter.endDate))
                val resultPatterns = trackingRepository.getTrackingFromDb(GetTrackingRequest(pref.values.loggedUser?.id
                        ?: 0, filter.initPatternDate, filter.endPatternDate))
                if (!result.isSuccessful or result.body().isNull()) return@runBlocking Result.failure()

                db.trackingDao().insert(*result.body()?.list?.toTypedArray()
                        ?: mutableListOf<Tracking>().toTypedArray())

                if (!resultPatterns.isSuccessful or resultPatterns.body().isNull()) return@runBlocking Result.failure()

                db.trackingDao().insert(*resultPatterns.body()?.list?.toTypedArray()
                        ?: mutableListOf<Tracking>().toTypedArray())


                return@runBlocking Result.success()
            }
        } catch (e: Exception) {
            ExceptionUtil.captureException(e)
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncTrackingWorker"

        @JvmStatic
        fun buildRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            return OneTimeWorkRequestBuilder<TrackingWorker>()
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