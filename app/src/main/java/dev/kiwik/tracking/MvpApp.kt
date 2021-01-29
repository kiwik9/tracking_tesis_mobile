package dev.kiwik.tracking

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.installations.FirebaseInstallationsApi
import net.danlew.android.joda.JodaTimeAndroid

class MvpApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        JodaTimeAndroid.init(this);
        instance = this


    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        lateinit var instance: MvpApp
            private set
        var currentActivity: AppCompatActivity? = null
    }
}