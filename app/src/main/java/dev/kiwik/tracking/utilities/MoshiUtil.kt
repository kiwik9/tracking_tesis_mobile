package dev.kiwik.tracking.utilities

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiUtil {
    val instance: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(CustomDateTimeAdapter())
        .build()
}