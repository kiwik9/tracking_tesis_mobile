package dev.kiwik.tracking.ui.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityMenuConfigBinding
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.location.LocationForegroundService
import dev.kiwik.tracking.preferences.Pref
import kotlinx.coroutines.runBlocking

class MenuConfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuConfigBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)
        setButtonFunctions()
    }

    private fun setButtonFunctions() {
        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdateProfile.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
        binding.updatePreferences.setOnClickListener {
            startActivity(Intent(this, PreferencesActivity::class.java))
        }

        binding.configTracking.setOnClickListener {
            startActivity(Intent(this, TrackingConfigurationActivity::class.java))
        }
    }

    private fun logout() {
        endService()
        val pref = Pref.getInstance()
        pref.values.loggedUser = null
        pref.update()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun endService() {
        val intentForeground = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${packageName}"), this, LocationForegroundService::class.java)
        stopService(intentForeground)
    }
}