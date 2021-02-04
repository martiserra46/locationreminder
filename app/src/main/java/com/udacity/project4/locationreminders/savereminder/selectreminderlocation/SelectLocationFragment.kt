package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.opengl.Visibility
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtils
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var poi: PointOfInterest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    private fun onLocationSelected() {
        if (poi != null) {
            _viewModel.apply {
                selectedPOI.value = poi
                reminderSelectedLocationStr.value = poi!!.name
                latitude.value = poi!!.latLng.latitude
                longitude.value = poi!!.latLng.longitude
            }
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            _viewModel.showSnackBarInt.value = R.string.select_poi
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        map.isMyLocationEnabled = true
        map.setOnPoiClickListener { poi -> onPoiClicked(poi) }
        FusedLocationProviderClient(requireActivity()).lastLocation
                .addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful) {
                val lat = task.result!!.latitude
                val lng = task.result!!.longitude
                val latLng = LatLng(lat, lng)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            }
        }
    }

    private fun onPoiClicked(poi: PointOfInterest) {
        map.clear()
        val markerOptions = MarkerOptions().apply {
            position(poi.latLng)
            title(poi.name)
        }
        map.addMarker(markerOptions).showInfoWindow()
        val circleOptions = CircleOptions().apply {
            center(poi.latLng)
            radius(GeofenceUtils.GEOFENCE_RADIUS.toDouble())
            strokeColor(Color.argb(255, 255, 0, 0))
            fillColor(Color.argb(64, 255, 0, 0))
            strokeWidth(4f)
        }
        map.addCircle(circleOptions)
        this.poi = poi
    }
}
