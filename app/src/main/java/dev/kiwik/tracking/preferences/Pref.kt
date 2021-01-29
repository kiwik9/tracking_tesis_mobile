package dev.kiwik.tracking.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.squareup.moshi.JsonAdapter
import dev.kiwik.tracking.MvpApp
import dev.kiwik.tracking.utilities.ExceptionUtil
import dev.kiwik.tracking.utilities.MoshiUtil

class Pref {

    private val moshi = MoshiUtil.instance
    private var preferences: SharedPreferences
    private var adapter: JsonAdapter<SessionData>
    lateinit var values: SessionData

    init {
        this.preferences = MvpApp.instance.applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        adapter = moshi.adapter(SessionData::class.java)
        load()
    }

    private fun load() {
        try {
            val jsonString = preferences.getString(KEY, "")
            jsonString?.let {
                values = if (it.isNotEmpty()) adapter.fromJson(it)!! else SessionData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onLoad", e)
        }

    }

    fun update() {
        try {
            val editor = preferences.edit()
            val jsonString = adapter.toJson(values)
            editor.putString(KEY, jsonString)
            editor.apply()
        } catch (e: Exception) {
            ExceptionUtil.captureException(e)
            Log.e(TAG, "update", e)
        }

    }

    fun clear() {
        values.loggedUser= null
        update()
    }

    companion object {

        private val TAG = Pref::class.java.simpleName
        private val KEY = SessionData::class.java.simpleName

        //{ singleton
        private var instance: Pref? = null

        @Synchronized
        fun getInstance(): Pref {
            return instance ?: Pref().also { instance = it }
        }
    }

}
