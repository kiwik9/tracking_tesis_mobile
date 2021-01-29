package dev.kiwik.tracking.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.squareup.moshi.JsonClass
import dev.kiwik.tracking.domain.db.converters.ListSubCategoryConverter


@JsonClass(generateAdapter = true)
@TypeConverters(ListSubCategoryConverter::class)
@Entity(tableName = "m_category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val description: String,
    val subCategories: List<SubCategory> = mutableListOf()
)

data class SubCategory(
    val id: Int,
    val name: String,
    val description: String,
    val categoryId: Int
)

data class CategoryCheck(
    val categoryId: Int,
    val subCategoryId: Int,
    val categoryName: String,
    val categoryDescription: String,
    var isSelected: Boolean,
    val isTitle: Boolean,
    val subCategoryName: String,
    val subCategoryDescription: String
)

@JsonClass(generateAdapter = true)
data class CategoryPreferences(
    val subCategoryId: Int,
    val categoryId: Int,
    val isSelected: Boolean
)
