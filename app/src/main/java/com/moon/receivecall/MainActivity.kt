package com.moon.receivecall

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions


class MainActivity : AppCompatActivity() {

    var locationManager: LocationManager? = null
    var latitude: Double? = null
    var longitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        intent?.run {
            val sender = getStringExtra("sender")
            val content = getStringExtra("contents")
            getCurrentLocation()
            Log.i(
                "MQ!",
                "sender: $sender, content:$content, latitude:$latitude, longitude:$longitude "
            )
            if (sender != null && content != null && latitude != null && longitude != null &&
                content.contains("@#")
            ) {
                Log.i("MQ!", "sendTextMessage")
                val url = "https://maps.google.com/?q=$latitude,$longitude"
                try {
                    SmsManager.getDefault().run {
                        sendTextMessage(sender, null, "currentLocation:$url", null, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val rationale = "Please provide location permission so that you can ..."
        val options: Permissions.Options = Permissions.Options()
            .setRationaleDialogTitle("Info")
            .setSettingsDialogTitle("Warning")
        Permissions.check(this, permissions, rationale, options, object : PermissionHandler() {
            override fun onGranted() {
//                Toast.makeText(this@MainActivity, "Permission granted.", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }

            override fun onDenied(context: Context?, deniedPermissions: ArrayList<String?>?) {
                Toast.makeText(this@MainActivity, "Permission denied.", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCurrentLocation() {
        getLatLng()?.run {
            Log.i("MQ!", "latitude:$latitude, longitude:$longitude")
            this@MainActivity.latitude = latitude
            this@MainActivity.longitude = longitude
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLatLng(): Location? {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        Log.i("MQ!", "getLatLng locationManager:$locationManager")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MQ!", "getLatLng permission denied")
            return null
        }
        return locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }


    companion object {
        const val REQUEST_CODE = 1000
        const val REQUEST_CODE_LOCATION = 2000
    }
}