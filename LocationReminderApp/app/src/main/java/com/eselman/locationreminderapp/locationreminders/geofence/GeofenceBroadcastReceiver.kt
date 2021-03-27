package com.eselman.locationreminderapp.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.eselman.locationreminderapp.R
import com.eselman.locationreminderapp.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.eselman.locationreminderapp.utils.remindersList
import com.eselman.locationreminderapp.utils.sendNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_GEOFENCE_EVENT) {
                val geofencingEvent = GeofencingEvent.fromIntent(intent)

                if (geofencingEvent.hasError()) {
                    val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                    Log.e(GeofencingConstants.TAG, errorMessage)
                    return
                }

                if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.v(GeofencingConstants.TAG, context.getString(R.string.geofence_entered))
                    val fenceId = when {
                        geofencingEvent.triggeringGeofences.isNotEmpty() ->
                            geofencingEvent.triggeringGeofences[0].requestId
                        else -> {
                            Log.e(GeofencingConstants.TAG, "No Geofence Trigger Found! Abort mission!")
                            return
                        }
                    }
                    val foundIndex = remindersList.indexOfFirst {
                        it.id == fenceId
                    }
                    if ( -1 == foundIndex ) {
                        Log.e(GeofencingConstants.TAG, "Unknown Geofence")
                        return
                    }

                   sendNotification(context, remindersList[foundIndex])
                }
            }
        }

    }
