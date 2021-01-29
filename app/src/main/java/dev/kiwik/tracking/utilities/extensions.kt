package dev.kiwik.tracking.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.kiwik.tracking.R
import java.util.*

fun AppCompatActivity.checkAndRequestPermission(permission: String, code: Int): Boolean { // todo: mejorar esta funcion
    if (hasPermission(permission)) return true
    requestPermissions(arrayOf(permission), code)
    return false
}

fun AppCompatActivity.checkAndRequestPermission(code: Int, vararg permissions: String): Boolean {
    val notPermitted = permissions.filter { !hasPermission(it) }
    if (notPermitted.isEmpty()) return true
    ActivityCompat.requestPermissions(this, notPermitted.toTypedArray(), code)
    return false
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun View.scaleAnimation(scaleX: Float, scaleY: Float, onStopCallback: (() -> Unit)? = null) {
    animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(200)
            .withEndAction {
                onStopCallback?.let { it() }
            }
}

fun Context.getTelephonyService(): TelephonyManager? {
    if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return null

    return getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}

fun Any?.isNull() = this == null
fun Any?.isNotNull() = !this.isNull()

@SuppressLint("MissingPermission")
fun Context.getDeviceId() = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)

fun Context.getDeviceUid(): String {
    val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
    val sharedPrefs = getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE)
    var uniqueID= sharedPrefs.getString(PREF_UNIQUE_ID, null)
    if (uniqueID == null) {
        uniqueID = UUID.randomUUID().toString()
        val editor = sharedPrefs.edit()
        editor.putString(PREF_UNIQUE_ID, uniqueID)
        editor.apply()
    }
    return uniqueID
}

fun Context.getConectivityService() =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.isConnected(): Boolean {
    val activeNetwork: NetworkInfo? = getConectivityService().activeNetworkInfo
    return activeNetwork?.isConnected ?: false
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
fun Context.getImei() = getTelephonyService()?.imei ?: ""

fun Context.getLocationService() = getSystemService(Context.LOCATION_SERVICE) as LocationManager

fun Context.isGpsEnabled() = getLocationService().isProviderEnabled(LocationManager.GPS_PROVIDER)

fun Context.getWifiService() =
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

fun Context.isWifiEnabled() = getWifiService().isWifiEnabled

@SuppressLint("MissingPermission")
fun Context.getPhoneNumber() = getTelephonyService()?.line1Number


@TargetApi(Build.VERSION_CODES.P)
@SuppressLint("MissingPermission")
fun Context.getSignalLevel() = getTelephonyService()?.signalStrength?.level?.toFloat() ?: 0f


fun Context.isMobileDataEnabled() = getConectivityService().isActiveNetworkMetered


fun Context.getBatteryLevel(): Int {
    val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    return batteryStatus?.let { intent ->
        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        (level / scale.toFloat() * 100).toInt()
    } ?: 0
}

fun Context.dpToPixel(size: Int): Int {
    val scale = resources.displayMetrics.density
    return (size * scale).toInt()
}

fun View.changeVisible(yes: Boolean) {
    this.visibility = if (yes) View.VISIBLE else View.GONE
}

fun AppCompatActivity.startNewActivity(activity: Class<out AppCompatActivity>, wantFinish: Boolean = false) {
    startActivity(Intent(this, activity))
    if (wantFinish) finish()
}

fun Context.getDrawableCompat(resId: Int): Drawable? {
    return ContextCompat.getDrawable(this, resId)
}

inline fun <T, reified S> Iterable<T>.reduce(first: S, operation: (acc: S, T) -> S): S {
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty collection can't be reduced.")
    var accumulator: S = first
    while(iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}


fun Context.showToast(@StringRes stringRes: Int? = null, text: String? = null, duration: Int = Toast.LENGTH_SHORT) {
    val toastText = stringRes?.let { getString(it) } ?: ""
    Toast.makeText(this, text ?: toastText, duration).show()
}