package dev.kiwik.tracking.utilities

import android.content.Context
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.repository.ResourceRepository
import dev.kiwik.tracking.repository.TrackingRepository
import dev.kiwik.tracking.viewmodel.factory.ResourceViewModelFactory
import dev.kiwik.tracking.viewmodel.factory.TrackingViewModelFactory


object InjectorUtils {

    private fun getTrackingRepository(context: Context): TrackingRepository {
        return TrackingRepository.getInstance(AppDatabase.getInstance().trackingDao())
    }

    fun provideTrackingViewModelFactory(context: Context): TrackingViewModelFactory {
        val repository = getTrackingRepository(context)
        return TrackingViewModelFactory(repository)
    }

    private fun getResourceRepository(context: Context): ResourceRepository {
        return ResourceRepository.getInstance(AppDatabase.getInstance().resourceDao())
    }

    fun provideResourceViewModelFactory(context: Context): ResourceViewModelFactory {
        val repository = getResourceRepository(context)
        return ResourceViewModelFactory(repository)
    }

}

