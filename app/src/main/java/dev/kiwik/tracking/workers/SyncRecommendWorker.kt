package dev.kiwik.tracking.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.kiwik.tracking.R
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.domain.entities.CategoryCheck
import dev.kiwik.tracking.domain.entities.Place
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.repository.ResourceRepository
import dev.kiwik.tracking.ui.activity.PlacesActivity
import dev.kiwik.tracking.utilities.ExceptionUtil
import dev.kiwik.tracking.utilities.isNull
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


class SyncRecommendWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

    private val json = GsonBuilder().setPrettyPrinting().create()
    val pref = Pref.getInstance()

    override suspend fun doWork(): Result {
        Log.v(SyncTokenWorker.TAG, "---- ${SyncTokenWorker.TAG} ----")
        return try {
            return runBlocking {
                val db = AppDatabase.getInstance()
                val repository = ResourceRepository.getInstance(db.resourceDao())

                val preferencesUser = getPreferencesUser(repository)
                val preference = preferencesUser.shuffled().first()

                val result =
                        repository.getPlaces(getUrlGoogle(ResourceRepository.getInstance(db.resourceDao()), preference))
                if (!result.isSuccessful or result.body()
                                .isNull()
                ) return@runBlocking Result.failure()

                val recommend = result.body()?.results?.let { getRecommendation(it, repository, preference) }

                recommend?.let { repository.insertPlace(it) }
                recommend?.let { sendNotification(it) }

                return@runBlocking Result.success()
            }
        } catch (e: Exception) {
            ExceptionUtil.captureException(e)
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun setPreferences(data: List<CategoryCheck>, pref: String): List<CategoryCheck> {
        val defaultJson = if (pref.isBlank() || pref == "Non preferences") "[]" else pref
        val collectionType: Type = object : TypeToken<Collection<CategoryCheck?>?>() {}.type
        val list: Collection<CategoryCheck> = json.fromJson(defaultJson, collectionType)
        data.forEach {
            it.isSelected =
                    list.find { sel -> sel.categoryId == it.categoryId && sel.subCategoryId == it.subCategoryId }?.isSelected
                            ?: false
        }
        return data
    }

    private fun getCheckCategories(list: List<Category>): List<CategoryCheck> {
        val categories = mutableListOf<CategoryCheck>()
        list.forEach {
            categories.add(CategoryCheck(0, 0, it.name, it.description, false, true, "", ""))
            categories.addAll(it.subCategories.map { sub ->
                CategoryCheck(
                        it.id,
                        sub.id,
                        it.name,
                        it.description,
                        false,
                        false,
                        sub.name,
                        sub.description
                )
            })
        }
        categories.add(CategoryCheck(0, 0, "", "", false, true, "", ""))

        return categories
    }

    private suspend fun getPreferencesUser(repository: ResourceRepository): List<CategoryCheck> {
        val preferencesJson = pref.values.loggedUser!!.preferences
        val categories = repository.getAllCategories()
        return setPreferences(getCheckCategories(categories), preferencesJson)
    }

    private suspend fun getUrlGoogle(repository: ResourceRepository, preference: CategoryCheck): String {
        val config = pref.values.trackingConfig

        val typeName = getTypeByPreferences(preference)
        val tracking = pref.values.lastLocation!!
        val locations = "location=${tracking.latitude},${tracking.longitude}"
        val radius = "&radius=${config.radius}"
        val type = "&type=$typeName"
        val base = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
        val key = "&key=AIzaSyArOJhB8wZEo_TAEsPLy6ZbMPf0k7aZedo"
        return "$base$locations$radius$type$key"
    }

    private fun getTypeByPreferences(pref: CategoryCheck): String {
        return when (pref.subCategoryId) {
            1 -> "amusement_park"
            2 -> "aquarium"
            3 -> "art_gallery"
            4 -> "pub"
            5 -> "bowling_alley"
            6 -> "casino"
            7 -> "library"
            8 -> "movie_theater"
            9 -> "book_store"
            10 -> "night_club"
            11 -> "museum"
            12 -> "zoo"
            13 -> "beauty_salon"
            14 -> "physiotherapist"
            15 -> "gym"
            16 -> "spa"
            17 -> "hair_care"
            18 -> "coffee"
            19 -> "bakery"
            20 -> "restaurant"
            21 -> "campground"
            22 -> "park"
            23 -> "florist"
            24 -> "clothing_store"
            else -> "shoe_store"
        }
    }

    private suspend fun getRecommendation(
            list: List<Place>,
            repository: ResourceRepository,
            preference: CategoryCheck
    ): Place? {
        val nonExistList = mutableListOf<Place>()

        list.forEach { place ->
            val exist = repository.getPlace(place.place_id)
            val type = getTypeByPreferences(preference)
            Log.e("PLACE", place.toString())
            val contains = place.types?.contains(type) ?: false
            if (exist.isNull() and contains) {
                nonExistList.add(place)
            }
        }

        Log.e("LISTA CON DATOS", list.size.toString())
        Log.e("LISTA PARA LLENAR", nonExistList.size.toString())

        return nonExistList.shuffled().firstOrNull()
    }


    private fun sendNotification(place: Place) {
        val intent = Intent(applicationContext, PlacesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
        )
        val channelId = ""
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Encontramos un sitio que te pude gustar")
                .setContentText(place.name)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(place.id, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "SyncAllWorker"

        @JvmStatic
        fun buildRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            return OneTimeWorkRequestBuilder<SyncRecommendWorker>()
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                    )
                    .setConstraints(constraints)
                    .build()
        }
    }
}