package com.eselman.locationreminderapp.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.eselman.locationreminderapp.locationreminders.data.dto.ReminderDTO
import com.eselman.locationreminderapp.locationreminders.data.dto.Result
import com.eselman.locationreminderapp.locationreminders.data.local.RemindersLocalRepository
import com.eselman.locationreminderapp.locationreminders.reminderslist.ReminderDataItem
import com.eselman.locationreminderapp.utils.remindersList
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                    intent
            )
        }
}

    override fun onHandleWork(intent: Intent) {
        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val result = remindersLocalRepository.getReminders()
            if(result is Result.Success<List<ReminderDTO>>) {
                remindersList.clear()
                remindersList.addAll((result.data).map { reminder ->
                    //map the reminder data from the DB to the be ready to be displayed on the UI
                    ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                    )
                })
            }

            addGeofences(remindersList)

            handleGeofencesTransition(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleGeofencesTransition(intent: Intent) {
        val geofencingClient = LocationServices.getGeofencingClient(this)

        val geofences = getGeoFences()

        geofences.forEach { landmark ->

            val geofence = Geofence.Builder()
                    .setRequestId(landmark.id)
                    .setCircularRegion(
                            landmark.latLong.latitude,
                            landmark.latLong.longitude,
                            GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

            val geofencePendingIntent =
                    PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                addOnCompleteListener {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                        addOnSuccessListener {
                            Log.d(GeofencingConstants.TAG, geofence.requestId)
                        }
                        addOnFailureListener {
                            if ((it.message != null)) {
                                Log.d(GeofencingConstants.TAG, it.message as String)
                            }
                        }
                    }
                }
            }
        }
    }
}

