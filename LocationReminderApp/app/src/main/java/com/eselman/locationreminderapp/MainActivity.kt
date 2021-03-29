package com.eselman.locationreminderapp

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.eselman.locationreminderapp.locationreminders.geofence.GeofenceBroadcastReceiver
import com.eselman.locationreminderapp.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.eselman.locationreminderapp.locationreminders.geofence.GeofencingConstants
import com.eselman.locationreminderapp.locationreminders.savereminder.SaveReminderFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private lateinit var view: View

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        view = findViewById(R.id.mainContainer)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    fun checkPermissionsAndStartGeofencing() {
        if (foregroundPermissionApproved()) {
            if (runningQOrLater) {
                if (backgroundPermissionApproved()) {
                    checkDeviceLocationSettingsAndStartGeofence()
                } else {
                    requestBackgroundLocationPermission()
                }
            } else {
                checkDeviceLocationSettingsAndStartGeofence()
            }
        } else {
            requestForegroundLocationPermission()
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @TargetApi(29)
    private fun backgroundPermissionApproved(): Boolean {
        return  PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    }


    private fun requestForegroundLocationPermission() {
        Log.d(GeofencingConstants.TAG, "Request foreground location permission")
        if (foregroundPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissionsArray,
            resultCode
        )
    }

    @TargetApi(29)
    private fun requestBackgroundLocationPermission() {
        Log.d(GeofencingConstants.TAG, "Request background location permission")
        if (backgroundPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val resultCode = REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissionsArray,
            resultCode
        )
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(GeofencingConstants.TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                   view,
                   R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
                intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
                GeofenceTransitionsJobIntentService.enqueueWork(this@MainActivity, intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(GeofencingConstants.TAG, "onRequestPermissionResult")

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        } else {
            when (requestCode) {
                REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE -> {
                    if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        ) {
                        if (runningQOrLater) {
                            if (backgroundPermissionApproved()) {
                                checkDeviceLocationSettingsAndStartGeofence()
                            } else {
                                requestBackgroundLocationPermission()
                            }
                        } else {
                            checkDeviceLocationSettingsAndStartGeofence()
                        }
                    } else {
                        Snackbar.make(
                            view,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.settings) {
                                startActivity(Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }.show()
                    }
                }

                REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE -> {
                    if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    } else {
                        Snackbar.make(
                            view,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.settings) {
                                startActivity(Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }.show()
                    }
                }
            }
        }
    }

    fun enableMapLocation(map: GoogleMap) {
        googleMap = map
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            googleMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                            arrayOf (Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE = 34
private const val REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE = 36
private const val REQUEST_LOCATION_PERMISSION = 100
