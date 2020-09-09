package com.example.gemastik

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var myLocation:Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        checkPermmison()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))

        checkPermmison()
    }

    private var accessLocation = 123
    private fun checkPermmison(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.
                checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),accessLocation)
                return
            }
        }
        getUserLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            accessLocation->{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getUserLocation()
                }else{
                    Toast.makeText(this,"We cannot access to your location",Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(){
        val myCurrentLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myCurrentLocation)
        // 3 detik, 3 meter
        val myThread = MyThread()
        myThread.start()
    }

    inner class MyLocationListener :LocationListener{
        init {
            myLocation = Location("Start")
            myLocation!!.longitude = 0.0
            myLocation!!.latitude = 0.0
        }
        override fun onLocationChanged(location: Location?) {
            myLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // TODO("Not yet implemented")
        }

        override fun onProviderEnabled(provider: String?) {
            // TODO("Not yet implemented")
        }

        override fun onProviderDisabled(provider: String?) {
            // TODO("Not yet implemented")
        }
    }

    inner class MyThread : Thread() {
        override fun run() {
            while(true){
                try{
                    runOnUiThread {
                        mMap.clear()
                        val my = LatLng(myLocation!!.latitude, myLocation!!.longitude)
                        mMap.addMarker(MarkerOptions().position(my).title("My Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(my))
                    }
                    sleep(1000)
                }catch (ex:Exception){}
            }
        }
    }

}