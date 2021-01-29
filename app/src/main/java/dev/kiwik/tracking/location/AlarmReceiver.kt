package dev.kiwik.tracking.location

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.WorkManager
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.getDefaultTracking
import dev.kiwik.tracking.domain.entities.toTracking
import dev.kiwik.tracking.workers.SyncTrackingWorker
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.Seconds
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        turnOnScreen(context)
        val db = AppDatabase.getInstance()
        startLocationForegroundService(context)
        runBlocking {
            syncLocations(context)
            setAlarm(context, false)
        }
    }

    @SuppressLint("BatteryLife")
    fun startLocationForegroundService(context: Context) {
        val intentForeground = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}"), context, LocationForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentForeground)
        } else {
            context.startService(intentForeground)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncLocations(context: Context) {
        Log.v(LocationIntentService.TAG, "Obteniendo tracking de db....")
        val db = AppDatabase.getInstance()
        val tmpTracking = db.trackingBaseDao().getAll()
        val now = DateTime.now()
        val lastLocations = tmpTracking.filter {
            (Seconds.secondsBetween(now, it.createdAt.toDateTime("yyyy-MM-dd HH:mm:ss")).seconds <= 30)
        }
        Log.v(LocationIntentService.TAG, "Ubicaciones obtenidas...${lastLocations.size}")
        val tracking = lastLocations.minBy { it.accuracy }?.toTracking()
            ?: getDefaultTracking()

        insertTracking(tracking)
        val ids = lastLocations.map { it.id!! }
        Log.v(LocationIntentService.TAG, "Eliminando de db....")
        db.trackingBaseDao().deleteByIds(*ids.toIntArray())
        initWorker(context)
    }

    private suspend fun insertTracking(tracking: Tracking) {
        val db = AppDatabase.getInstance()
        Log.v(LocationIntentService.TAG, "LATITUDE: ${tracking.latitude}")
        Log.v(LocationIntentService.TAG, "LONGITUDE: ${tracking.longitude}")
        Log.v(LocationIntentService.TAG, "ACCURACY: ${tracking.accuracy}")
        tracking.trackingId = UUID.randomUUID().toString()
        db.trackingDao().insert(tracking)
    }

    private fun initWorker(context: Context) {
        val workerManager = WorkManager.getInstance(context)
        workerManager.enqueue(SyncTrackingWorker.buildRequest())
    }


    companion object {
        const val TAG = "AlarmReceiver"
        private const val CUSTOM_INTENT = "io.creavity.suitesolmar.intent.action.ALARM"

        private var screenWakeLock: PowerManager.WakeLock? = null

        @SuppressLint("InvalidWakeLockTag")
        private fun turnOnScreen(context: Context) {
            val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            screenWakeLock = (pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "SCREEN_WAKE_LOCK"
            )) as PowerManager.WakeLock
            screenWakeLock!!.acquire()
            screenWakeLock!!.release()
        }

        fun cancelAlarm(ctx: Context) {
            val alarm = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(getPendingIntent(ctx))
        }


        fun setAlarm(ctx: Context, force: Boolean) {
            turnOnScreen(ctx)
            cancelAlarm(ctx)
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val delay: Long = 5000
            var whenDelay: Long = System.currentTimeMillis()

            if (!force) {
                whenDelay += delay
            }
            val pendingIntent = getPendingIntent(ctx)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(whenDelay, pendingIntent),
                pendingIntent
            )

        }

        @SuppressLint("BatteryLife")
        private fun getPendingIntent(ctx: Context): PendingIntent {
            val alarmIntent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + ctx.packageName), ctx, AlarmReceiver::class.java
            )
            alarmIntent.action = CUSTOM_INTENT
            return PendingIntent.getBroadcast(
                ctx,
                0,
                alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }
}
