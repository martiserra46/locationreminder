package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object GeofenceUtils {

    private const val TAG = "GeofenceUtils"

    private const val INTENT_EXTRA_REMINDER = "intent_extra_reminder"
    const val GEOFENCE_RADIUS = 50f

    @SuppressLint("MissingPermission")
    fun addGeofence(context: Context, reminderDataItem: ReminderDataItem) {
        val geofencingRequest = geofencingRequest(reminderDataItem)
        val pendingIntent = pendingIntent(context, reminderDataItem)
        val geofencingClient = LocationServices.getGeofencingClient(context)
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.i(TAG, "addGeofence: Geofence added")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addGeofence: An error occurred adding the geofence", e)
            }
    }

    private fun geofencingRequest(reminderDataItem: ReminderDataItem): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence(reminderDataItem))
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
    }

    private fun geofence(reminderDataItem: ReminderDataItem): Geofence {
        return Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(reminderDataItem.latitude!!, reminderDataItem.longitude!!, GEOFENCE_RADIUS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    private fun pendingIntent(
        context: Context,
        reminderDataItem: ReminderDataItem
    ): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.putExtra(INTENT_EXTRA_REMINDER, reminderDataItem)
        return PendingIntent.getBroadcast(context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
}