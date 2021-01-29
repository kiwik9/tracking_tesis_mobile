package dev.kiwik.tracking.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import dev.kiwik.tracking.location.LocationForegroundService
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.isNull

class GoogleTrackingUtility(private val context: Context) {

    private lateinit var mGoogleApiClient: GoogleApiClient
    private val pref by lazy {
        Pref.getInstance()
    }

    init {
        buildGoogleApiClient()
        startService()
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient.connect()
    }

    @SuppressLint("BatteryLife")
    private  fun startService() {
        if (pref.values.loggedUser.isNull()) return

        val intentForeground = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}"), context, LocationForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentForeground)
        } else {
            context.startService(intentForeground)
        }
    }
}