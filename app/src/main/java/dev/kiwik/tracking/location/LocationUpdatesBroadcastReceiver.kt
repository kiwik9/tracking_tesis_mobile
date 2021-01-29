package dev.kiwik.tracking.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.WorkManager
import com.google.android.gms.location.LocationResult
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.TrackingBase
import dev.kiwik.tracking.domain.entities.toTracking
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.ExceptionUtil
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.workers.SyncRecommendWorker
import dev.kiwik.tracking.workers.SyncTrackingWorker
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val pref = Pref.getInstance()
        if (pref.values.loggedUser.isNull()) return

        val action = intent.action
        if (ACTION_PROCESS_UPDATES == action) {
            val result = LocationResult.extractResult(intent)
            val db = AppDatabase.getInstance()
            val trackingBaseDao = db.trackingBaseDao()
            if (result != null) {
                runBlocking {
                    val locations = result.locations
                    locations.forEach {
                        val track = getTracking(it)
                        Log.i(TAG, "------ REGISTRANDO EN DATABASE -------")
                        Log.i(
                                TAG,
                                "Ubicacion: ${it.latitude}, ${it.longitude} -> accuracy ${it.accuracy}"
                        )
                        trackingBaseDao.insertWith(track)
                        Log.i(TAG, "------ -------")
                    }
                    if (locations.isNotEmpty()) {
                        initDateRecommend(getTracking(locations.lastOrNull()).toTracking(), context)
                        initWorker(context)
                    }
                }
            } else {
                ExceptionUtil.captureException("Ubicación no encontrado")
            }
        } else {
            ExceptionUtil.captureException("Intent distinto $action")
        }

    }

    private fun String.toDate(): DateTime {
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        return formatter.parseDateTime(this)
    }

    private fun initDateRecommend(tracking: Tracking, context: Context) {
        val pref = Pref.getInstance()
        pref.values.lastLocation = tracking
        val config = pref.values.trackingConfig
        val user = pref.values.loggedUser
        if (user?.preferences == "Non preferences" || user?.preferences.isNullOrBlank()) {
            return
        }
        if (pref.values.lastRecommend == null) {
            pref.values.lastRecommend = DateTime.now().plusMinutes((config.startTime..config.endTime).shuffled().first()).toString("yyyy-MM-dd HH:mm")
        }
        val last = pref.values.lastRecommend!!.toDate()
        val now = DateTime.now()
        if (now.isAfter(last)) {
            pref.values.lastRecommend = DateTime.now().plusMinutes((config.startTime..config.endTime).shuffled().first()).toString("yyyy-MM-dd HH:mm")
            pref.update()
        } else {
            return
        }
        pref.update()
        val workerManager = WorkManager.getInstance(context)
        workerManager.enqueue(SyncRecommendWorker.buildRequest())
    }

    private fun initWorker(context: Context) {
        val workerManager = WorkManager.getInstance(context)
        workerManager.enqueue(SyncTrackingWorker.buildRequest())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTracking(location: Location?): TrackingBase {
        val interval = 5
        val pref = Pref.getInstance()
        val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        return TrackingBase(
                userId = pref.values.loggedUser?.id ?: 0,
                trackingId = UUID.randomUUID().toString(),
                latitude = location?.latitude ?: 0.0,
                speed = location?.speed ?: 0f,
                altitude = location?.altitude?.toFloat() ?: 0f,
                bearing = location?.bearing ?: 0f,
                accuracy = location?.accuracy ?: 999f,
                longitude = location?.longitude ?: 0.0,
                interval = interval,
                createdAt = now
        )
    }

    companion object {
        private const val TAG = "LUBroadcastReceiver"
        const val ACTION_PROCESS_UPDATES =
                "pruebatracking.action.PROCESS_UPDATES"
    }
}