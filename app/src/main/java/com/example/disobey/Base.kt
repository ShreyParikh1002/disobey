package com.example.disobey

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.disobey.Fragments.CameraFragment
import com.example.disobey.Fragments.DashboardFragment
import com.example.disobey.Fragments.LeaderboardFragment
import com.example.disobey.Fragments.MainFragment
import com.example.disobey.Fragments.TradeFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class Base : AppCompatActivity() {

    lateinit var chipNavigationBarMain : ChipNavigationBar;

    private val PERMISSION_REQUEST_CODE = 1
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor?=null
    lateinit var stepsTaken : TextView
    lateinit var coinsEarned : TextView
    private var running = false
    private var totalSteps = 0f
    lateinit var pref: SharedPreferences
    private var initialSteps = 0
    private var disobeySteps = 0
    private var dailySteps = 0
    private var coins = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        window.statusBarColor = getColor(android.R.color.transparent)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
//        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
//        initialSteps= pref.getInt("initialSteps",-1)
//        disobeySteps= pref.getInt("disobeySteps",0)
//        dailySteps= pref.getInt("dailySteps",0)

        if (!hasPermissions()) {
            requestPermissions()
        }

        chipNavigationBarMain = findViewById<ChipNavigationBar>(R.id.main_nav)
        chipNavigationBarMain.setItemSelected(
            R.id.bottom_nav_map,
            true
        )
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(
                R.id.fragment_container,
                MainFragment()
            ).commit()
        bottomMenu()
    }
    private fun bottomMenu() {
        chipNavigationBarMain.setOnItemSelectedListener { id ->
            val fragment: Fragment = when (id) {
                R.id.bottom_nav_map -> MainFragment()
                R.id.bottom_nav_leaderboards -> LeaderboardFragment()
                R.id.bottom_nav_dashboard->DashboardFragment()
                R.id.bottom_nav_trade->TradeFragment()
                R.id.bottom_nav_cam->CameraFragment()
                else -> throw IllegalArgumentException("Invalid menu item ID")
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
    private fun hasPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (!allPermissionsGranted) {
                var shouldShowRationale = false
                for (permission in PERMISSIONS) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        shouldShowRationale = true
                        break
                    }
                }

                if (shouldShowRationale) {
                    showRationaleDialog()
                } else {
                    showSettingsDialog()
                }
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Following permissions are required for this app to function properly\nLocation for map\nActivity tracking for steps\nCamera and microphone for 3D try-on\nStorage to save images.\nPlease grant the permissions.")
            .setPositiveButton("OK") { _, _ -> requestPermissions() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("These permissions are required for this app to function properly\nLocation for map\nActivity tracking for steps\nCamera and microphone for 3D try-on .\nPlease grant the permissions in the app settings.")
            .setPositiveButton("Settings") { _, _ -> openAppSettings() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
//    override fun onResume() {
//        super.onResume()
//        running = true
//
//        // TYPE_STEP_COUNTER:  A constant describing a step counter sensor
//        // Returns the number of steps taken by the user since the last reboot while activated
//        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
//        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//
//        if (stepSensor == null) {
//            // show toast message, if there is no sensor in the device
//            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
//        } else {
//            // register listener with sensorManager
//            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//    override fun onSensorChanged(event: SensorEvent?) {
//
//        if (running) {
//
//            //get the number of steps taken by the user.
//            totalSteps = event!!.values[0]
//
//            var currentSteps = totalSteps.toInt()
//
//            val myEdit = pref.edit()
//            if(initialSteps==-1){
//                myEdit.putInt("initialSteps",currentSteps)
//                initialSteps=currentSteps
//            }
////            else if(currentSteps<initialSteps || currentSteps<disobeySteps){
////                disobeySteps+=currentSteps
////                initialSteps=currentSteps
////            }
//            else{
//                dailySteps+=currentSteps-initialSteps
//                disobeySteps+=currentSteps-initialSteps
//                initialSteps=currentSteps
//            }
//            myEdit.putInt("disobeySteps",disobeySteps)
//            myEdit.putInt("dailySteps",dailySteps)
//            myEdit.putInt("initialSteps",initialSteps)
////            if(disobeySteps>=100 && !tryOn.isEnabled){
//////                Toast.makeText(this, "hurray", Toast.LENGTH_SHORT).show()
//////                tryOn.isClickable=true
//////                tryOn.alpha=1.0f
////                var dialog=Dialog(this)
////                dialog.setContentView(R.layout.hurray)
////                dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
////                dialog.show()
////                dialog.findViewById<ImageButton>(R.id.close).setOnClickListener {
////                    dialog.dismiss()
////                }
////                tryOn.isEnabled=true
////
////
////            }
//
//            // set current steps in textview
////            stepsTaken.text = ("$dailySteps")
////            coins=dailySteps/100
////            coinsEarned.text = ("${coins}")
//            myEdit.commit()
//        }
//    }
//    //
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
////        TODO("Implemented not required")
//    }
}