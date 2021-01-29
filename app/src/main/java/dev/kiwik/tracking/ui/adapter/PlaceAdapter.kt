package dev.kiwik.tracking.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.PlaceItemLayoutBinding
import dev.kiwik.tracking.domain.entities.Place

class PlaceAdapter :
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    private val places = mutableListOf<Place>()
    private var mListener: OnItemClickListener<Place>? = null

    private lateinit var mLayoutInflater: LayoutInflater

    fun submitList(data: List<Place>) {
        places.clear()
        places.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener<Place>) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        val binding = PlaceItemLayoutBinding.inflate(mLayoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = places.size

    inner class ViewHolder(private val binding: PlaceItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Place) {

            binding.btnInfo.setOnClickListener {
                mListener?.invoke(item)
            }

            binding.namePlace.text = item.name
            binding.nameCategories.text = item.vicinity

            Glide.with(binding.imagePlace).load(item.icon)
                .placeholder(R.drawable.ic_place)
                .error(R.drawable.ic_place)
                .into(binding.imagePlace)

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(places[position])

}
