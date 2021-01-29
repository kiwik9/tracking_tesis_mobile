package dev.kiwik.tracking.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.kiwik.tracking.repository.ResourceRepository
import dev.kiwik.tracking.viewmodel.ResourceViewModel

class ResourceViewModelFactory(private val repository: ResourceRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ResourceViewModel(repository)
            as T
}