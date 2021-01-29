package dev.kiwik.tracking.ui.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityPlacesBinding
import dev.kiwik.tracking.ui.adapter.PlaceAdapter
import dev.kiwik.tracking.utilities.InjectorUtils
import dev.kiwik.tracking.viewmodel.ResourceViewModel

class PlacesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesBinding
    private lateinit var adapter: PlaceAdapter

    private val viewModel: ResourceViewModel by viewModels {
        InjectorUtils.provideResourceViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)

        setButtonFunction()
        initRV()
    }

    private fun initRV() {
        adapter = PlaceAdapter()
        binding.rvPlaces.adapter = adapter
        viewModel.getAllPlace().observe(this) {
            adapter.submitList(it)
        }
        adapter.setOnItemClickListener {
            val map = "https://www.google.com/maps/search/?api=1&query=Google&query_place_id=${it.place_id}"
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(map))
            startActivity(i)
        }
    }

    private fun setButtonFunction() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}