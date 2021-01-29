package dev.kiwik.tracking.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessaging
import dev.kiwik.tracking.domain.api.UpdateTokenRequest
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.repository.ResourceRepository
import dev.kiwik.tracking.utilities.ExceptionUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class SyncTokenWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.v(TAG, "---- $TAG ----")
        return try {
            return runBlocking {
                val pref = Pref.getInstance()
                val db = AppDatabase.getInstance()
                val resourceRepository = ResourceRepository.getInstance(db.resourceDao())

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) return@addOnCompleteListener
                    val token = task.result
                    Log.d(TAG, "Token : $token")
                    runBlocking {
                        try {
                            resourceRepository.updateFirebaseToken(
                                UpdateTokenRequest(
                                    userId = pref.values.loggedUser!!.id,
                                    token = token ?: "No token"
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                return@runBlocking Result.success()
            }
        } catch (e: Exception) {
            ExceptionUtil.captureException(e)
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val TAG = "SYNC_TOKEN_WORKER"

        @JvmStatic
        fun buildRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<SyncTokenWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
        }
    }
}