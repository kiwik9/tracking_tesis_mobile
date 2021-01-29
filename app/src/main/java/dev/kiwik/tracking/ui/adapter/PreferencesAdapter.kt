package dev.kiwik.tracking.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ButtonItemBinding
import dev.kiwik.tracking.databinding.PreferenceItemBinding
import dev.kiwik.tracking.databinding.TitleNameBinding
import dev.kiwik.tracking.domain.entities.CategoryCheck
import dev.kiwik.tracking.domain.entities.CategoryPreferences
import java.lang.reflect.Type

typealias OnItemClickListener<T> = (item: T) -> Unit

class PreferenceAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val preference = mutableListOf<CategoryCheck>()
    private var mListener: OnItemClickListener<CircularProgressButton>? = null
    private val json = GsonBuilder().setPrettyPrinting().create()

    private lateinit var mLayoutInflater: LayoutInflater

    private val TITLE = 1
    private val BUTTON = 3
    private val CHECK = 2

    fun submitList(data: List<CategoryCheck>, pref: String) {
        preference.clear()
        preference.addAll(setPreferences(data, pref))
        notifyDataSetChanged()
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

    fun setOnItemClickListener(listener: OnItemClickListener<CircularProgressButton>) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return when (viewType) {
            TITLE -> {
                val binding = TitleNameBinding.inflate(mLayoutInflater, parent, false)
                TitleViewHolder(binding)
            }
            BUTTON -> {
                val binding = ButtonItemBinding.inflate(mLayoutInflater, parent, false)
                ButtonViewHolder(binding)
            }
            else -> {
                val binding = PreferenceItemBinding.inflate(mLayoutInflater, parent, false)
                PreferenceViewHolder(binding)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (preference[position].isTitle && position == itemCount - 1) BUTTON else if (preference[position].isTitle) TITLE else CHECK
    }

    override fun getItemCount() = preference.size

    fun getList() = preference

    fun getItemSelected() = preference.filter { !it.isTitle and it.isSelected }.count()

    fun getJsonList() = json.toJson(preference.filter { it.isSelected }.map {
        CategoryPreferences(
            it.subCategoryId,
            it.categoryId,
            it.isSelected
        )
    })


    inner class PreferenceViewHolder(private val binding: PreferenceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryCheck, position: Int) {
            if (item.isSelected) {
                binding.card.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
                binding.name.setTextColor(Color.WHITE)
            } else {
                binding.card.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                binding.name.setTextColor(Color.parseColor("#5364E8"))
            }

            binding.card.setOnClickListener {
                preference[position].isSelected = !item.isSelected
                notifyDataSetChanged()
            }
            binding.name.text = item.subCategoryName

            Glide.with(binding.image)
                .load(item.getPhoto())
                .into(binding.image)
            // binding.checkItem.text = item.subCategoryName
        }
    }

    fun CategoryCheck.getPhoto(): Int {
        return when (this.subCategoryId) {
            1 -> R.drawable.ic_carrusel
            2 -> R.drawable.ic_pecera
            3 -> R.drawable.ic_art_gallery
            4 -> R.drawable.ic_cocktail
            5 -> R.drawable.ic_bolos
            6 -> R.drawable.ic_dados
            7 -> R.drawable.ic_library
            8 -> R.drawable.ic_cine
            9 -> R.drawable.ic_book_store
            10 -> R.drawable.ic_discotheque
            11 -> R.drawable.ic_museun
            12 -> R.drawable.ic_zoo
            13 -> R.drawable.ic_salon_de_belleza__1_
            14 -> R.drawable.ic_fisioterapeuta
            15 -> R.drawable.ic_gym_station
            16 -> R.drawable.ic_spa
            17 -> R.drawable.ic_peluqueria
            18 -> R.drawable.ic_cafetera
            19 -> R.drawable.ic_panaderia
            20 -> R.drawable.ic_comida
            21 -> R.drawable.ic_tienda
            22 -> R.drawable.ic_parque
            23 -> R.drawable.ic_floristeria
            24 -> R.drawable.ic_ropa
            else -> R.drawable.ic_tacones_altos
        }
    }


    inner class ButtonViewHolder(private val binding: ButtonItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryCheck) {
            binding.btnUpdate.setOnClickListener {
                mListener?.invoke(binding.btnUpdate)
            }
            binding.btnUpdate.text = "Actualizar"
        }
    }

    inner class TitleViewHolder(private val binding: TitleNameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryCheck) {
            binding.title.text = item.categoryName
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PreferenceAdapter.PreferenceViewHolder -> holder.bind(preference[position], position)
            is PreferenceAdapter.TitleViewHolder -> holder.bind(preference[position])
            is PreferenceAdapter.ButtonViewHolder -> holder.bind(preference[position])
            else -> Unit
        }
    }
}