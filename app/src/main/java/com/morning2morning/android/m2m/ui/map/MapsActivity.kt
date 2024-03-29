package com.morning2morning.android.m2m.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.databinding.ActivityMapsBinding
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.customviews.RegularTextView
import java.io.IOException

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geocoder: Geocoder
    private val ACCESS_LOCATION_REQUEST_CODE = 10001
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest

    private lateinit var selectedLocationTextView: RegularTextView

    private lateinit var selectedLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geocoder = Geocoder(this)

        selectedLocationTextView = binding.selectedLocationTextView

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        binding.confirmButton.setOnClickListener {
            Pref(this).putPref(Constants.USER_DELIVERY_ADDRESS, selectedLocation)
            CustomToast().showToastMessage(this, "Location updated successfully!")
            finish()
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL


        mMap.setOnMapClickListener(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
            zoomToUserLocation()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_REQUEST_CODE)
            }
        }

    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
        }
    }


    private fun zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val locationTask: Task<Location> = fusedLocationProviderClient!!.lastLocation
            locationTask.addOnSuccessListener(OnSuccessListener<Location> { location ->
                if (location != null){
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
                    mMap.addMarker(MarkerOptions().position(latLng))

                    try {
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (addresses.size > 0) {
                            val address = addresses[0]
                            val streetAddress = address.getAddressLine(0)
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(streetAddress)
                                    .draggable(true)
                            )
                            selectedLocation = streetAddress
                            selectedLocationTextView.text = streetAddress
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    override fun onMapClick(it: LatLng) {
        mMap.clear()
        try {
            val addresses = geocoder!!.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.size > 0) {
                val address = addresses[0]
                val streetAddress = address.getAddressLine(0)
                mMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title(streetAddress)
                        .draggable(true)
                )
                selectedLocation = streetAddress
                selectedLocationTextView.text = streetAddress
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}