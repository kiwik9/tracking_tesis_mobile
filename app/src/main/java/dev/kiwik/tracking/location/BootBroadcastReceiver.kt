package dev.kiwik.tracking.location

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            AlarmReceiver.setAlarm(context, false)
            startLocationForegroundService(context)
            Log.e(TAG, "PASO")
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

    companion object {

        private val TAG = BootBroadcastReceiver::class.java.simpleName
    }
}

