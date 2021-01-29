package dev.kiwik.tracking.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityTrackingConfigurationBinding
import dev.kiwik.tracking.domain.entities.TrackingConfig
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.isNull

class TrackingConfigurationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackingConfigurationBinding

    private lateinit var configuration: TrackingConfig
    private val pref by lazy {
        Pref.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)

        setData()
        setButtonFunctions()
    }

    private fun setButtonFunctions() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnUpdate.setOnClickListener {
            updatePreferences()
        }

        binding.btnNotificationRangeInformation.setOnClickListener {
            MaterialDialog(this).show {
                setTheme(R.style.appTheme_materialdialog)
                title(text = "Rango de recomendaciones")
                positiveButton(text = "Ok") {
                    it.dismiss()
                }
                cornerRadius(16f)
                message(text = "Esta preferencia es para definir entre cuanto tiempo desea recibir notificaciones de recomendaciones " +
                        "la cual la aplicacion tomara encuenta para enviarle notificaciones en ese rango de minutos que desee.")
            }
        }

        binding.btnRadiusNotifications.setOnClickListener {
            MaterialDialog(this).show {
                setTheme(R.style.appTheme_materialdialog)
                title(text = "Radio de preferencias")
                positiveButton(text = "Ok") {
                    it.dismiss()
                }
                cornerRadius(16f)
                message(text = "Esta preferencia define el radio de busqueda de recomendaciones de su ubicacion, " +
                        "por ejemplo si esta en 75 metros el aplicativo le enviara recomendacion de lugares mas cercanos hasta 75 metros.")
            }
        }
    }

    private fun updatePreferences() {
        val radius = binding.editRadius.text.toString().toIntOrNull()
        val initNotificationTime = binding.editInitNotification.text.toString().toIntOrNull()
        val endNotificationTime = binding.editEndNotification.text.toString().toIntOrNull()
        if (!validatePref()) return
        pref.values.trackingConfig = configuration.copy(radius = radius!!, startTime = initNotificationTime!!, endTime = endNotificationTime!!)
        pref.update()
        Toast.makeText(this, "Se guardaron los datos.", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun validatePref(): Boolean {
        binding.rangeNotificationErrorTxt.isVisible = false
        binding.radiusErrorTxt.isVisible = false

        val radius = binding.editRadius.text.toString().toIntOrNull()
        val initNotificationTime = binding.editInitNotification.text.toString().toIntOrNull()
        val endNotificationTime = binding.editEndNotification.text.toString().toIntOrNull()
        var rangeError = ""
        var radioError = ""
        if (radius.isNull()) {
            radioError = "Este campo es necesario"
            binding.radiusErrorTxt.isVisible = true
            binding.radiusErrorTxt.text = radioError
        }

        if (initNotificationTime.isNull() or endNotificationTime.isNull()) {
            rangeError = "Este campo es necesario"
            binding.rangeNotificationErrorTxt.isVisible = true
            binding.rangeNotificationErrorTxt.text = rangeError
        }

        if (radius.isNull() or initNotificationTime.isNull() or endNotificationTime.isNull()) return false

        if (radius!! < 50 || radius > 500) {
            radioError = "El rango debe ser entre 50 a 500 metros"
            binding.radiusErrorTxt.isVisible = true
            binding.radiusErrorTxt.text = radioError
        }

        if (endNotificationTime!! <= initNotificationTime!!) {
            rangeError = "No pueden ser iguales o el primero mayor al ultimo."
            binding.rangeNotificationErrorTxt.isVisible = true
            binding.rangeNotificationErrorTxt.text = rangeError
        }

        if (initNotificationTime <= 0 || initNotificationTime > 60 || endNotificationTime <= 0 || endNotificationTime > 60) {
            radioError = "El rango debe ser entre 1 a 60 minutos"
            binding.rangeNotificationErrorTxt.isVisible = true
            binding.rangeNotificationErrorTxt.text = radioError
        }

        if (radius < 50 || radius > 500 || endNotificationTime <= initNotificationTime || initNotificationTime <= 0 || initNotificationTime > 60 || endNotificationTime <= 0 || endNotificationTime > 60) return false
        return true
    }

    private fun setData() {
        configuration = pref.values.trackingConfig
        binding.editRadius.setText(configuration.radius.toString())
        binding.editInitNotification.setText(configuration.startTime.toString())
        binding.editEndNotification.setText(configuration.endTime.toString())
    }
}