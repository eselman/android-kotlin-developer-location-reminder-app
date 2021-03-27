
package com.eselman.locationreminderapp.locationreminders.geofence

import android.content.Context
import com.eselman.locationreminderapp.R
import com.eselman.locationreminderapp.locationreminders.reminderslist.ReminderDataItem
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

data class LandmarkDataObject(val id: String, val name: String?, val latLong: LatLng)

internal object GeofencingConstants {
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    const val GEOFENCE_RADIUS_IN_METERS = 1000f
    const val TAG = "Geofences"
}

val geofences = mutableListOf<LandmarkDataObject>()

fun addGeofences(reminders: List<ReminderDataItem>) {
    geofences.clear()
    reminders.forEach { reminder ->
        val landmarkDataObject = LandmarkDataObject(reminder.id, reminder.title, LatLng(reminder.latitude!!, reminder.longitude!!))
        geofences.add(landmarkDataObject)
    }
}

fun getGeoFences() = geofences
