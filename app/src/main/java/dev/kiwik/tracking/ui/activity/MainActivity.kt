package dev.kiwik.tracking.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityMainBinding
import dev.kiwik.tracking.domain.entities.FilterParamTracking
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.ui.utils.GoogleTrackingUtility
import dev.kiwik.tracking.utilities.InjectorUtils
import dev.kiwik.tracking.utilities.isNotNull
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.viewmodel.TrackingViewModel
import dev.kiwik.tracking.workers.SyncAllWorker
import dev.kiwik.tracking.workers.SyncTokenWorker
import dev.kiwik.tracking.workers.TrackingWorker
import kotlinx.android.synthetic.main.filter_tracking.view.*
import org.joda.time.DateTime
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    var dateInit = ""
    var dateEnd = ""
    var dateInitPattern = ""
    var dateEndPattern = ""
    private lateinit var dateTextView: TextView
    var isInit = true

    private lateinit var trackingUtil: GoogleTrackingUtility
    private val pref by lazy {
        Pref.getInstance()
    }

    private val trackingViewModel: TrackingViewModel by viewModels {
        InjectorUtils.provideTrackingViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackingUtil = GoogleTrackingUtility(this)
        pref.values.filterParamTracking = FilterParamTracking()
        pref.update()
        isLogged()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)
        updateFirebaseToken()
        setData()
        insertPreferences()
        syncAllData()
        initMap()
        setButtonFunction()

        initLocationListener()
    }

    private fun insertPreferences() {
        val user = pref.values.loggedUser
        if (user?.preferences == "Non preferences" || user?.preferences.isNullOrBlank()) {
            MaterialDialog(this@MainActivity).show {
                setTheme(R.style.appTheme_materialdialog)
                title(text = "Agregar preferencias")
                positiveButton(text = "Agregar Preferencias") {
                    it.dismiss()
                    startActivity(Intent(this@MainActivity, PreferencesActivity::class.java))
                }
                cornerRadius(16f)
                message(text = "Es necesario agregar las preferencias para poder seguir usando la aplicacion.")
            }
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }


    private fun syncAllData() {
        val workManager = WorkManager.getInstance(this)
        val request = SyncAllWorker.buildRequest()
        workManager.enqueue(request)
    }

    private fun setButtonFunction() {
        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, MenuConfActivity::class.java))
        }
        binding.btnPlaces.setOnClickListener {
            startActivity(Intent(this, PlacesActivity::class.java))
        }
        binding.btnSearch.setOnClickListener {
            dialogFilterBuilder()
        }
    }

    private fun filterDataFromDb() {
        val filter = pref.values.filterParamTracking
        trackingViewModel.setFilterTracking(filter)

        val workManager = WorkManager.getInstance(this)
        val request = TrackingWorker.buildRequest()
        workManager.enqueue(request)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(request.id)
            .observe(this) { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    binding.pgIntent.isVisible = false
                    binding.layoutLeyend.isVisible = true
                } else if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
                    binding.pgIntent.isVisible = true
                } else if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
                    binding.pgIntent.isVisible = false
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        val user = pref.values.loggedUser
        binding.txtTitle.text = "Bienvenido, ${user?.name}"
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        resetMap(googleMap)
        trackingViewModel.getTracking().observe(this) { list ->
            mMap.clear()
            setTrackingTrace(list.toMutableList())
            trackingViewModel.getTrackingPattern().observe(this) { pattern ->
                setPatternTrackingTrace(pattern.toMutableList())
            }
        }

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful and it.result.isNotNull()) {
                val loc = it.result!!
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            loc.latitude,
                            loc.longitude
                        ), 18f
                    )
                )
            }
        }
    }

    private fun dialogFilterBuilder() {
        val dialog = AlertDialog.Builder(this).create()
        dialog.apply {
            val dialogLayout = layoutInflater.inflate(R.layout.filter_tracking, null, false).apply {

                dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                this.btCloseFilter.setOnClickListener {
                    dateInit = ""
                    dateEnd = ""
                    dialog.dismiss()
                }

                this.edit_DateInit.setOnClickListener {
                    isInit = true
                    this@MainActivity.dateTextView = this.edit_DateInit
                    showDataTimePicker()
                }

                this.edit_DateEnd.setOnClickListener {
                    isInit = false
                    this@MainActivity.dateTextView = this.edit_DateEnd
                    showDataTimePicker()
                }

                this.btFilterFilter.setOnClickListener {

                    if (dateInit.isBlank() or dateEnd.isBlank()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Es necesario llenar las dos fechas.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    val pref = Pref.getInstance()
                    pref.values.filterParamTracking =
                        FilterParamTracking(dateInit, dateEnd, dateInitPattern, dateEndPattern)
                    pref.update()
                    dateInit = ""
                    dateEnd = ""
                    filterDataFromDb()
                    mMap.clear()
                    dialog.dismiss()
                }
            }
            this.setView(dialogLayout)
        }.show()
    }

    private fun showDataTimePicker() {
        SlideDateTimePicker.Builder(supportFragmentManager)
            .setListener(slideDateTimeListener)
            .setInitialDate(Date())
            .setMaxDate(Date())
            .build()
            .show()
    }

    private val slideDateTimeListener: SlideDateTimeListener = object : SlideDateTimeListener() {
        override fun onDateTimeSet(date: Date?) {
            val dateTime = DateTime(date!!.time)
            val dateTimePattern = DateTime(date.time).plusDays(-1)
            val dateString = dateTime.toString("yyyy-MM-dd HH:mm:ss")
            val datePatternString = dateTimePattern.toString("yyyy-MM-dd HH:mm:ss")
            if (isInit) {
                dateInit = dateString
                dateInitPattern = datePatternString
            } else {
                dateEnd = dateString
                dateEndPattern = datePatternString
            }
            this@MainActivity.dateTextView.text = dateString
        }

        override fun onDateTimeCancel() {

        }
    }

    private fun resetMap(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
    }

    private fun setTrackingTrace(locations: MutableList<Tracking>) {
        mMap.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        mMap.apply {
            val start = locations.firstOrNull()
            val end = locations.lastOrNull()
            if (start.isNotNull()) {
                addMarker(
                    MarkerOptions()
                        .position(LatLng(start!!.latitude, start.longitude))
                        .title("Inicio")
                )
            }

            if (end.isNotNull()) {
                addMarker(
                    MarkerOptions()
                        .position(LatLng(end!!.latitude, end.longitude))
                        .title("Fin")
                )
            }

            //var distance = 0.0
            for (i in locations.indices) {
                val positions = mutableListOf<Tracking>()
                positions.add(locations[i])
                if (i != locations.size - 1) {
                    positions.add(locations[i + 1])
                }
                //  distance += calculateDistance(positions.first().latitude,positions.last().latitude, positions.first().longitude, positions.last().longitude )
                this.addPolyline(
                    PolylineOptions()
                        .color(getColorBySpeed(positions.firstOrNull()?.speed ?: 0f))
                        .add(*positions.map { LatLng(it.latitude, it.longitude) }.toTypedArray())
                )
            }

            if (start.isNotNull()) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            start!!.latitude,
                            start.longitude
                        ), 32f
                    )
                )
            }

            this.setOnPolylineClickListener(this@MainActivity)
            this.setOnPolygonClickListener(this@MainActivity)
            this.setOnMyLocationClickListener(this@MainActivity)
        }
    }

    private fun setPatternTrackingTrace(locations: MutableList<Tracking>) {
        mMap.apply {
            for (i in locations.indices) {
                val positions = mutableListOf<Tracking>()
                positions.add(locations[i])
                if (i != locations.size - 1) {
                    positions.add(locations[i + 1])
                }
                this.addPolyline(
                    PolylineOptions()
                        .color(Color.RED)
                        .add(*positions.map { LatLng(it.latitude, it.longitude) }.toTypedArray())
                )
            }
            this.setOnPolylineClickListener(this@MainActivity)
            this.setOnPolygonClickListener(this@MainActivity)
            this.setOnMyLocationClickListener(this@MainActivity)
        }
    }


    private fun getColorBySpeed(speed: Float): Int {
        val kph = speed * 3.6
        return when {
            kph <= 9.5 -> Color.parseColor("#059033")
            else -> Color.parseColor("#e2943a")
        }
    }


    private fun updateFirebaseToken() {
        if (pref.values.loggedUser.isNull()) return
        val workManager = WorkManager.getInstance(this)
        val request = SyncTokenWorker.buildRequest()
        workManager.enqueue(request)
    }

    override fun onResume() {
        super.onResume()
        isLogged()
        insertPreferences()
        setData()
    }

    private fun isLogged() {
        if (pref.values.loggedUser.isNull()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onPolylineClick(p0: Polyline?) {
    }

    override fun onPolygonClick(p0: Polygon?) {
    }

    override fun onMyLocationClick(@NonNull location: Location) {
    }

    private fun initLocationListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val snackBar = Snackbar.make(
            binding.root,
            "Es necesario activar el gps para que funcione correctamente la aplicacion.",
            Snackbar.LENGTH_LONG
        ).setAction("Activar") {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        if (!gpsStatus) snackBar.show()
    }

}