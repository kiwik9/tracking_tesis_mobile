package dev.kiwik.tracking.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dev.kiwik.tracking.domain.entities.FilterParamTracking
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.repository.TrackingRepository


class TrackingViewModel(private val repository: TrackingRepository) : ViewModel(){

    private val filterParamTracking: MutableLiveData<FilterParamTracking> by lazy {
        MutableLiveData(FilterParamTracking())
    }

    fun setFilterTracking(filter : FilterParamTracking) {
        this.filterParamTracking.value = filter
    }

    fun getTracking(): LiveData<List<Tracking>> {
        return Transformations.switchMap(filterParamTracking) { filter ->
            repository.getTracking(filter.initDate, filter.endDate)
        }
    }

    fun getTrackingPattern(): LiveData<List<Tracking>> {
        return Transformations.switchMap(filterParamTracking) { filter ->
            repository.getTracking(filter.initPatternDate, filter.endPatternDate)
        }
    }

}