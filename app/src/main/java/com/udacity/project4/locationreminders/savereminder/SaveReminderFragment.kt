package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtils
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    companion object {
        private const val RC_FINE_LOCATION_ACCESS: Int = 1
        private const val RC_BACKGROUND_LOCATION_ACCESS: Int = 2
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            if (checkFineLocationPermission()) {
                navigateToSelectLocationFragment()
            } else {
                requestFineLocationPermission()
            }
        }

        binding.saveReminder.setOnClickListener {
            if (checkBackgroundLocationPermission()) {
                saveReminder()
            } else {
                requestBackgroundLocationPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun checkFineLocationPermission() : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkBackgroundLocationPermission() : Boolean {
        return Build.VERSION.SDK_INT < 29 ||
                ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestFineLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), RC_FINE_LOCATION_ACCESS)
    }

    private fun requestBackgroundLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), RC_BACKGROUND_LOCATION_ACCESS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_FINE_LOCATION_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigateToSelectLocationFragment()
            }
        } else if (requestCode == RC_BACKGROUND_LOCATION_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveReminder()
            }
        }
    }

    private fun navigateToSelectLocationFragment() {
        _viewModel.navigationCommand.value = NavigationCommand.To(
            SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
        )
    }

    private fun saveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        val success = _viewModel.validateAndSaveReminder(reminderDataItem)
        if (success) GeofenceUtils.addGeofence(requireContext(), reminderDataItem)
    }
}
