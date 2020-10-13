@file:Suppress("DEPRECATION", "SameParameterValue")

package com.example.gemastik

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.android.synthetic.main.bottomsheet.view.*
import org.json.JSONArray
import java.io.IOException
import com.google.maps.android.heatmaps.WeightedLatLng
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class MapFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var rootView: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var rbPemerintah: RadioButton? = null
    private var rbRealtime: RadioButton? = null
    private var rbPrediksi: RadioButton? = null

    private var circleJaksel: Circle? = null
    private var circleJakbar: Circle? = null
    private var circleJakut: Circle? = null
    private var circleJaktim: Circle? = null
    private var circleDepok: Circle? = null
    private var circleBekasi: Circle? = null
    private var circleCirebon: Circle? = null
    private var circleBogor: Circle? = null
    private var circleCilegon: Circle? = null
    private var circleTangsel: Circle? = null
    private var circleTangerang:Circle? = null

    private var heatmapOverlay: TileOverlay? = null
    private var stateMap: Int = 1

    private var autocompleteFragment: AutocompleteSupportFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        rootView = inflater.inflate(R.layout.activity_maps, container, false)
        setHasOptionsMenu(true)

        inisialisasiLokasi()
        createLocationRequest()
        inisialisasiBS()
        inisialisasiAutoComplete()
        initializeRb()
        inisialisasiDatePicker()


        return rootView
    }

    companion object{
//        var TAG = MapFragment::class.java.simpleName
        private const val ARG_POSITION: String = "position"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
//        private const val PLACE_PICKER_REQUEST = 3
//        private const val AUTOCOMPLETE_REQUEST_CODE = 4
        fun newInstance(): MapFragment{
            val fragment = MapFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setOnMarkerClickListener(this)

        setUpMap()

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }

        modePemerintah()
    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
//                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(activity as AppCompatActivity)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage!!)
        }
        return addressText
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(activity as Activity)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(activity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                val place = PlacePicker.getPlace(activity, data)
//                var addressText = place.name.toString()
//                addressText += "\n" + place.address.toString()
//
//                placeMarkerOnMap(place.latLng)
//            }
//        }

    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

//    private fun loadPlacePicker() {
//        val builder = PlacePicker.IntentBuilder()
//
//        try {
//            startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
//        } catch (e: GooglePlayServicesRepairableException) {
//            e.printStackTrace()
//        } catch (e: GooglePlayServicesNotAvailableException) {
//            e.printStackTrace()
//        }
//    }

    private fun initializeRb(){
        rbPemerintah = rootView.findViewById(R.id.rb_pemerintah)
        rbRealtime = rootView.findViewById(R.id.rb_realtime)
        rbPrediksi = rootView.findViewById(R.id.rb_prediksi)

        rbPemerintah!!.setOnClickListener {
            rbPemerintah!!.isChecked = true
            rbRealtime!!.isChecked = false
            rbPrediksi!!.isChecked = false

            if(stateMap != 1){
                heatmapOverlay!!.remove()
                modePemerintah()
                stateMap = 1
            }
        }

        rbRealtime!!.setOnClickListener {
            rbPemerintah!!.isChecked = false
            rbRealtime!!.isChecked = true
            rbPrediksi!!.isChecked = false

            if(stateMap != 2 && stateMap != 3){
                circleTangerang!!.remove()
                circleTangsel!!.remove()
                circleJakut!!.remove()
                circleJakbar!!.remove()
                circleJaksel!!.remove()
                circleJaktim!!.remove()
                circleBogor!!.remove()
                circleCilegon!!.remove()
                circleCirebon!!.remove()
                circleBekasi!!.remove()
                circleDepok!!.remove()
                modeHeatmap()
                stateMap = 2
            }
        }

        rbPrediksi!!.setOnClickListener {
            rbPemerintah!!.isChecked = false
            rbRealtime!!.isChecked = false
            rbPrediksi!!.isChecked = true

            if(stateMap != 3 && stateMap != 2){
                circleTangerang!!.remove()
                circleTangsel!!.remove()
                circleJakut!!.remove()
                circleJakbar!!.remove()
                circleJaksel!!.remove()
                circleJaktim!!.remove()
                circleBogor!!.remove()
                circleCilegon!!.remove()
                circleCirebon!!.remove()
                circleBekasi!!.remove()
                circleDepok!!.remove()
                modePrediksi()
                stateMap = 3
            }
        }
    }

    private fun modePemerintah(){
        buatCircle()
    }

    private fun modeHeatmap(){
        val data = generateHeatMapData()

        val heatMapProvider = HeatmapTileProvider.Builder()
            .weightedData(data) // load our weighted data
            .radius(50) // optional, in pixels, can be anything between 20 and 50
            .maxIntensity(1000.0) // set the maximum intensity
            .build()

        heatmapOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(heatMapProvider))
    }

    private fun modePrediksi(){
        modeHeatmap()
    }

    private fun buatCircle(){
        val datanya = generateCircleData()

        for (i in 0 until datanya.size){
            val namaTempat = datanya[i].getString("namaTempat")
            val lat = datanya[i].getDouble("latitude")
            val lon = datanya[i].getDouble("longitude")
            val luas = datanya[i].getDouble("luas") * 2

            val circleOptions = CircleOptions()
                .center(LatLng(lat, lon))
                .radius(luas)
                .fillColor(Color.argb(128, 255, 0, 0))
                .strokeWidth(0.0F)

            when (namaTempat){
                "jakarta utara" -> circleJakut = map.addCircle(circleOptions)
                "jakarta selatan" -> circleJaksel = map.addCircle(circleOptions)
                "jakarta barat" -> circleJakbar = map.addCircle(circleOptions)
                "jakarta timur" -> circleJaktim = map.addCircle(circleOptions)
                "depok" -> circleDepok = map.addCircle(circleOptions)
                "bekasi" -> circleBekasi = map.addCircle(circleOptions)
                "cirebon" -> circleCirebon = map.addCircle(circleOptions)
                "bogor" -> circleBogor = map.addCircle(circleOptions)
                "cilegon" -> circleCilegon = map.addCircle(circleOptions)
                "tangerang" -> circleTangerang = map.addCircle(circleOptions)
                "tangsel" -> circleTangsel = map.addCircle(circleOptions)
            }
        }
    }

    // return data dari json dataset
    private fun generateCircleData(): ArrayList<JSONObject>{
        val data = ArrayList<JSONObject>()

        val jsonData = getJsonDataFromAsset("datasetPetaSebaran.json")
        jsonData?.let {
            for (i in 0 until it.length()) {
                val entry = it.getJSONObject(i)

                data.add(entry)
            }
        }
        return data
    }

    //return data dari json dataset
    private fun generateHeatMapData(): ArrayList<WeightedLatLng> {
        val data = ArrayList<WeightedLatLng>()

        val jsonData = getJsonDataFromAsset("datasetfathan.json")
        jsonData?.let {
            for (i in 0 until it.length()) {
                val entry = it.getJSONObject(i)
                val lat = entry.getDouble("latitude")
                val lon = entry.getDouble("longitude")
                val density = entry.getDouble("density")

                if (density != 0.0) {
                    val weightedLatLng = WeightedLatLng(LatLng(lat, lon), density)
                    data.add(weightedLatLng)
                }
            }
        }
        return data
    }

    //ini fungsi buat load .json dari folder asset
    private fun getJsonDataFromAsset(fileName: String): JSONArray? {
        return try {
            val jsonString = context!!.assets.open(fileName).bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //inisialisasi lokasi dari pengguna
    private fun inisialisasiLokasi(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                // TODO: 09/09/20 last locationnya DISINI YA
//                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
    }

    private fun inisialisasiBS(){
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // handle onSlide
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
////                    BottomSheetBehavior.STATE_COLLAPSED -> Toast.makeText(activity, "STATE_COLLAPSED", Toast.LENGTH_SHORT).show()
////                    BottomSheetBehavior.STATE_EXPANDED -> Toast.makeText(activity, "STATE_EXPANDED", Toast.LENGTH_SHORT).show()
////                    BottomSheetBehavior.STATE_DRAGGING -> Toast.makeText(activity, "STATE_DRAGGING", Toast.LENGTH_SHORT).show()
////                    BottomSheetBehavior.STATE_SETTLING -> Toast.makeText(activity, "STATE_SETTLING", Toast.LENGTH_SHORT).show()
////                    BottomSheetBehavior.STATE_HIDDEN -> Toast.makeText(activity, "STATE_HIDDEN", Toast.LENGTH_SHORT).show()
////                    else -> Toast.makeText(activity, "OTHER_STATE", Toast.LENGTH_SHORT).show()
//                }
            }
        })
    }

    private fun inisialisasiAutoComplete(){
        Places.initialize(context!!, getString(R.string.google_maps_key))

        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        autocompleteFragment = childFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment
        autocompleteFragment!!.setPlaceFields(fields)

        autocompleteFragment!!.setOnPlaceSelectedListener(object:com.google.android.libraries.places.widget.listener.PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place selected: " + place.name)
            }
            override fun onError(p0: Status) {
                Log.d("Maps", "An error occurred:")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun inisialisasiDatePicker(){
        val tvDate:TextView = rootView.findViewById(R.id.tv_hari)
        val tvJam:TextView = rootView.findViewById(R.id.tv_jam)

        val c = Calendar.getInstance()
        val yearr = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        val jam = c.get(Calendar.HOUR_OF_DAY)
        val menit = c.get(Calendar.MINUTE)

        tvJam.text = "$jam:$menit"
        tvDate.text = "$day - $month - $yearr"

        tvDate.setOnClickListener {
            val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // set tv nya ke yang dipilih
                tvDate.text = "$dayOfMonth - $monthOfYear - $year"
            }, yearr, month, day)
            dpd.show()
        }

        tvJam.setOnClickListener {
            val tpd = TimePickerDialog(context!!, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                tvJam.text = "$hourOfDay:$minute"
            }, jam, menit, true)
            tpd.show()
        }
    }
    
}