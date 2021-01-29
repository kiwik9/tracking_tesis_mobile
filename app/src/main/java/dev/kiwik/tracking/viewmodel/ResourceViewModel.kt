package dev.kiwik.tracking.viewmodel

import androidx.lifecycle.ViewModel
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.repository.ResourceRepository
import kotlinx.coroutines.runBlocking


class ResourceViewModel(private val repository: ResourceRepository) : ViewModel() {

    fun getAllCategories(): List<Category> = runBlocking { repository.getAllCategories() }

    fun getAllPlace()  = repository.getPlaces()


}