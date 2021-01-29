package dev.kiwik.tracking.utilities

import com.crashlytics.android.Crashlytics
import  dev.kiwik.tracking.BuildConfig

object ExceptionUtil {
    fun captureException(throwable: Throwable) {
        Crashlytics.logException(throwable)
        if (BuildConfig.DEBUG) throwable.printStackTrace()
    }

    fun captureException(log: String) {
        Crashlytics.logException(RuntimeException(log))
    }

}
