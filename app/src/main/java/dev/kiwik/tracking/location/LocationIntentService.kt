package dev.kiwik.tracking.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.work.WorkManager
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.getDefaultTracking
import dev.kiwik.tracking.domain.entities.toTracking
import dev.kiwik.tracking.workers.SyncTrackingWorker
import dev.kiwik.tracking.preferences.Pref
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.math.abs

fun String.toDateTime(format: String): DateTime {
    return DateTimeFormat.forPattern(format).parseDateTime(this)
}

inline fun <T> Iterable<T>.sumBy(selector: (T) -> Double): Double {
    var sum: Double = 0.0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}


class LocationIntentService : JobIntentService() {

    private val pref = Pref.getInstance()

    @SuppressLint("MissingPermission")
    override fun onHandleWork(@NonNull intent: Intent) {
        Log.v(TAG, "Iniciando alarm...")
        AlarmReceiver.setAlarm(this, false)
        runBlocking {
            syncLocations()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncLocations() {
        Log.v(TAG, "Obteniendo tracking de db....")
        val db = AppDatabase.getInstance()
        val tmpTracking = db.trackingBaseDao().getAll()
        val now = DateTime.now()
        val lastLocations = tmpTracking.filter {
            (Seconds.secondsBetween(now, it.createdAt.toDateTime("yyyy-MM-dd HH:mm:ss")).seconds <= 30)
        }
        Log.v(TAG, "Ubicaciones obtenidas...${lastLocations.size}")
        val tracking = lastLocations.minBy { it.accuracy }?.toTracking()
            ?: getDefaultTracking()

        insertTracking(tracking)
        val ids = lastLocations.map { it.id!! }
        Log.v(TAG, "Eliminando de db....")
        db.trackingBaseDao().deleteByIds(*ids.toIntArray())
    }


    private fun syncLocationsWS() {
        runBlocking {
            val tmp = mutableListOf<Tracking>()
            tmp.addAll(pref.values.currentLocations)

            val now = DateTime.now()

            var lastLocations = tmp.filter {
                (Seconds.secondsBetween(now, it.createdAt.toDateTime("yyyy-MM-dd HH:mm:ss")).seconds <= 30)
            }
            if (lastLocations.map { it.accuracy }.toSet().size == 1) {
                Log.v(TAG, "Robando tracking 30 segundos antes")
                lastLocations = tmp.filter {
                    Seconds.secondsBetween(it.createdAt.toDateTime("yyyy-MM-dd HH:mm:ss"), now).seconds <= 60
                }
            }

            Log.v(TAG, "Tenemos ${lastLocations.size} ubicaciones por procesar")
            Log.v(TAG, "latitud: ${lastLocations.map { it.latitude }.joinToString(", ")}")
            Log.v(TAG, "longitud: ${lastLocations.map { it.longitude }.joinToString(", ")}")
            Log.v(TAG, "accuracy: ${lastLocations.map { it.accuracy }.joinToString(", ")}")

            val maxError = lastLocations.map { it.accuracy }.max()

            val weigth = { e: Double, max: Double -> 1 - (e / (max * 1.5)) }

            if (lastLocations.isEmpty()) return@runBlocking
            val weights = lastLocations.map { weigth(it.accuracy.toDouble(), maxError!!.toDouble()) }

            val weigthSum = weights.sum()

            if (weigthSum == 0.0) {
                insertTracking(lastLocations.first())
                return@runBlocking
            }

            val lat = lastLocations.zip(weights).sumBy { (tracking, weight) -> tracking.latitude * weight } / weigthSum
            val lng = lastLocations.zip(weights).sumBy { (tracking, weight) -> tracking.longitude * weight } / weigthSum

            val tracking = lastLocations.minBy { it.accuracy }
            tracking?.latitude = lat
            tracking?.longitude = lng

            val error = lastLocations.zip(weights).map { (it, weight) ->
                val results = FloatArray(3)
                Location.distanceBetween(it.latitude, it.longitude, lat, lng, results)
                abs(results[0] - it.accuracy) * weight
            }.sum() / weigthSum

            tracking?.accuracy = error.toFloat()

            insertTracking(tracking!!)
            val indexOf = tmp.indexOf(lastLocations.first())

            Log.v(TAG, "ESTE SOY ANTES DE pref ${pref.values.currentLocations.size}")

            pref.values.currentLocations =
                Collections.synchronizedList(pref.values.currentLocations.subList(indexOf, pref.values.currentLocations.lastIndex))
            pref.update()

            Log.v(TAG, "ESTE SOY DESPUES DE BORRAR ${pref.values.currentLocations.size}")
        }
    }

    private suspend fun insertTracking(tracking: Tracking) {
        val db = AppDatabase.getInstance()
        Log.v(TAG, "LATITUDE: ${tracking.latitude}")
        Log.v(TAG, "LONGITUDE: ${tracking.longitude}")
        Log.v(TAG, "ACCURACY: ${tracking.accuracy}")
        tracking.trackingId = UUID.randomUUID().toString()
        db.trackingDao().insert(tracking)
        initWorker()
    }

    private fun initWorker() {
        val workerManager = WorkManager.getInstance(applicationContext)
        workerManager.enqueue(SyncTrackingWorker.buildRequest())
    }


    companion object {
        const val TAG = "LocationIntentService"

        /* Give the Job a Unique Id */
        private const val JOB_ID = 1000

        fun enqueueWork(ctx: Context, intent: Intent) {
            enqueueWork(ctx, LocationIntentService::class.java, JOB_ID, intent)
        }
    }

}