package dev.kiwik.tracking.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.utilities.hasPermission


class LocationForegroundService : JobIntentService() {
    private lateinit var locationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        startForeground(2, getNotification())
    }

    private fun hasLocationPermission() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun startLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient.requestLocationUpdates(locationRequest, getPendingIntent())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //startForeground(2, getNotification())
        Log.v(TAG, "On create")
        Log.v(TAG, "Tiene permisos: ${hasLocationPermission()}")
        val db = AppDatabase.getInstance()
        if (hasLocationPermission()) {
            startLocationUpdates()
        }
        return Service.START_STICKY
    }

    override fun onHandleWork(intent: Intent) {

    }

    fun getNotification(): Notification {
        val channelId = "io.creavity.suitesolmar"
        val channelName = "My Background Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_NONE
            )
            val manager = (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle("Important background job")
                .setSound(null, AudioManager.STREAM_RING)
                .setContentText("tracking....")
                .setOngoing(true)
                .build()

    }

    @SuppressLint("BatteryLife")
    private fun getPendingIntent(): PendingIntent? { // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".
        // TODO(developer): uncomment to use PendingIntent.getService().
        //        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
        //        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
        //        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName"), this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        val TAG = LocationForegroundService::class.java.simpleName
    }

}