package dev.kiwik.tracking.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import dev.kiwik.tracking.domain.api.GetTrackingRequest
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.domain.entities.SubCategory
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.repository.ResourceRepository
import dev.kiwik.tracking.repository.TrackingRepository
import dev.kiwik.tracking.utilities.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class SyncAllWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.v(SyncTokenWorker.TAG, "---- ${SyncTokenWorker.TAG} ----")
        return try {
            return runBlocking {
                val pref = Pref.getInstance()
                val db = AppDatabase.getInstance()
                val repository = ResourceRepository.getInstance(db.resourceDao())
                val result = repository.getCategories()
                if(!result.isSuccessful or result.body().isNull()) return@runBlocking Result.failure()
                val list = addSubCategories(result.body()?.categories, result.body()?.subCategories)
                db.resourceDao().insertCategories(*list.toTypedArray())
                return@runBlocking Result.success()
            }
        } catch (e: Exception) {
            ExceptionUtil.captureException(e)
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun addSubCategories(categories: List<Category>?, subs : List<SubCategory>?): List<Category>{
        if(categories.isNullOrEmpty()) return emptyList()
        val subCategories = subs?: listOf()
        val newList = mutableListOf<Category>()
        categories.forEach {
            newList.add(it.copy(subCategories = subCategories.filter { sub -> sub.categoryId == it.id }))
        }
        return newList
    }

    companion object {
        private const val TAG = "SyncAllWorker"

        @JvmStatic
        fun buildRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<SyncAllWorker>()
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