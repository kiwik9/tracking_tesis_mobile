package dev.kiwik.tracking.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.kiwik.tracking.repository.TrackingRepository
import dev.kiwik.tracking.viewmodel.TrackingViewModel

class TrackingViewModelFactory (private val repository: TrackingRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = TrackingViewModel(repository)
            as T
}