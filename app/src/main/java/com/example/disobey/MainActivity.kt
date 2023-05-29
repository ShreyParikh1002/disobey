package com.example.disobey

import android.Manifest
import android.animation.Animator
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import org.json.JSONObject
import kotlin.math.abs


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mapView: MapView
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
//    ----------------------------------------------------------------------------------------------
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor?=null
    //    two line in on create also
    private var running = false
    private var totalSteps = 0f
    lateinit var cameraButton: ImageButton
    lateinit var recenterButton: ImageButton
    lateinit var avatarButton: ImageButton
    lateinit var scan: Button
    lateinit var stepsTaken :TextView
    lateinit var coinsEarned :TextView
    lateinit var chipNavigationBarMain : ChipNavigationBar;

    lateinit var pref:SharedPreferences
    private var initialSteps = 0
    private var disobeySteps = 0
    private var dailySteps = 0
    private var coins = 0
    val timeoutSet = mutableSetOf(24)
//    private var rebooted=true


    var annotationApi : AnnotationPlugin? = null
    lateinit var annotaionConfig : AnnotationConfig
    val layerID = "disobeyAnnotations";
    var pointAnnotationManager : PointAnnotationManager? = null
    var markerList :ArrayList<PointAnnotationOptions> = ArrayList()
    var specialMarkerList :ArrayList<PointAnnotationOptions> = ArrayList()
    var latitudeList : ArrayList<Double> = ArrayList()
    var longitudeList : ArrayList<Double> = ArrayList()
    var annotationAdded = false

    var buttonPressed = false
    val featureList = mutableListOf<Feature>()

    var currentLatitude=0.0
    var currentLongitude=0.0
    var countnum=0
//    ----------------------------------------------------------------------------------------------
//    private fun makeRequest() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
//                android.Manifest.permission.ACTIVITY_RECOGNITION,
//                android.Manifest.permission.CAMERA,
//                android.Manifest.permission.INTERNET,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
//                android.Manifest.permission.RECORD_AUDIO
//            ),
//            RECORD_REQUEST_CODE
//        )
//    }

    private val PERMISSION_REQUEST_CODE = 1
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

//    private fun setupPermissions() {
//        val permission = ContextCompat.checkSelfPermission(
//            this,
//            android.Manifest.permission.ACCESS_FINE_LOCATION
//        )
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Location permission denied")
//            makeRequest()
//        }
//    }
//    ----------------------------------------------------------------------------------------------
        var one=R.drawable.loneshark
        var two=R.drawable.darksmoke
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chipNavigationBarMain = findViewById(R.id.main_nav)
        chipNavigationBarMain.setItemSelected(
            R.id.bottom_nav_map,
            true
        )
        chipNavigationBarMain.setOnItemSelectedListener { id ->
            when (id) {
//                R.id.bottom_nav_dashboard -> {
//                    // Handle navigation to Activity1
//                    Handler().postDelayed({
//                        val intent = Intent(this@MainActivity, Dashboard::class.java)
//                        startActivity(intent)
//                        overridePendingTransition(0,0)
//                    },300)
//                    true
//                }
//                R.id.bottom_nav_leaderboards -> {
//                    val intent = Intent(this@MainActivity, Leaderboard::class.java)
//                    startActivity(intent)
//                    true
//                }
                R.id.bottom_nav_cam -> {
                    val intent = Intent(this@MainActivity, snapCam::class.java)
                    intent.putExtra("Type","1")
                    startActivity(intent)
                    true
                }

                // Add more cases for other navigation items if needed
                else -> false
            }

        }
//        setupPermissions()
        if (!hasPermissions()) {
            requestPermissions()
        }

//        code snippet for chip navigation, currently executed at the end of scheduling intent
//        still preserved so as to test impact on app startup once app is completed
//........................................................................
//        chipNavigationBar = findViewById(R.id.nav);
//        chipNavigationBar.setItemSelected(R.id.nav,
//                true);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container,
//                        new Home()).commit();
//        bottomMenu();
//........................................................................




//        scheduling after 10 seconds of launch
//.................................................................................................
    val alarmy = getSystemService(ALARM_SERVICE) as AlarmManager
    var triggerTime = System.currentTimeMillis() + (5 * 60 + 30) * 60 * 1000
//            India is 5 hr 30 mins ahead so added it for IST conversion
//            subtracting the remainder ((triggerTime)%(60*60*1000)) converts to to nearest hour

    triggerTime=(((24*60+1)*60*1000)-(triggerTime% (24*60*60*1000)))+triggerTime- ((5 * 60 + 30) * 60 * 1000)
//    println("timeis"+triggerTime)
//        Toast.makeText(this, Long.toString(triggerTime), Toast.LENGTH_SHORT).show();
    //        Toast.makeText(this, Long.toString(triggerTime), Toast.LENGTH_SHORT).show();
    val broadcast = Intent(this@MainActivity, dailyReceiver::class.java)
    val pi =
        PendingIntent.getBroadcast(this@MainActivity, 100, broadcast, PendingIntent.FLAG_MUTABLE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmy.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
    }

    stepsTaken = findViewById(R.id.stepCount)
        coinsEarned = findViewById(R.id.coinCount)
//        mapView = MapView(this)
//        setContentView(mapView)
        cameraButton=findViewById(R.id.cameraButton)
        recenterButton=findViewById(R.id.recenterButton)
        avatarButton=findViewById(R.id.avatar)
//        scan=findViewById(R.id.scan)
//
//        scan.setOnClickListener {
////            val intent = Intent(this, snapCam::class.java)
////            startActivity(intent)
//            createLatLongForMarker()
//            timeoutSet.clear()
//            buttonPressed=true
//        }
        cameraButton.setOnClickListener {
            val intent = Intent(this, snapCam::class.java)
            intent.putExtra("Type","1")
            startActivity(intent)
        }

//        avatarButton.setOnClickListener {
//            Handler().postDelayed({
//                val DashboardIntent = Intent(this, Dashboard::class.java)
//                startActivity(DashboardIntent)
//                overridePendingTransition(0,0)
//            },1000)
//
//        }
//    recenterButton.setOnClickListener {
//        mapView.getMapboxMap().flyTo(
//            CameraOptions.Builder()
//                .zoom(17.0)
//                .build()
//        )
//        initLocationComponent()
//        setupGesturesListener()
//
//    }
//        mapView = findViewById(R.id.mapView)
////        createLatLongForMarker();
//        onMapReady()
//        mapView.location.addOnIndicatorPositionChangedListener(currentLocation)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        initialSteps= pref.getInt("initialSteps",-1)
        disobeySteps= pref.getInt("disobeySteps",0)
        dailySteps= pref.getInt("dailySteps",0)
//        if(disobeySteps>100){
//            tryOn.isEnabled=true
//        }
        stepsTaken.text = ("${dailySteps}")
        coins=dailySteps/100
    coinsEarned.text = ("${coins}")

        val mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Checking GPS is enabled
        val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!mGPS){
               Toast.makeText(this,"Please switch on GPS/Location",Toast.LENGTH_SHORT).show()
        }
//        Toast.makeText(this, "initial $initialSteps", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, "disobey $disobeySteps", Toast.LENGTH_SHORT).show()
//        createLatLongForMarker()
//    ******************************************************************************************************************************************************************************************


// Add each point in the array as a Feature to the FeatureCollection


//    mapView.getMapboxMap().getStyle()?.addLayer(
//        SymbolLayer("a", "b").withProperties(
//    PropertyFactory.iconImage("your-icon-image-name")
//    ))
//    ******************************************************************************************************************************************************************************************
    }
//    ----------------------------------------------------------------------------------------------
//    private fun onMapReady() {
//
//        mapView.getMapboxMap().setCamera(
//            CameraOptions.Builder()
//                .zoom(17.0)
//                .build()
//        )
//        mapView.getMapboxMap().loadStyle(
//            style(styleUri="mapbox://styles/shrey1002/clhre4h8x01z101pgaekh31wy/draft")
//            {
//                initLocationComponent()
//                setupGesturesListener()
//                annotationApi = mapView?.annotations
//                annotaionConfig = AnnotationConfig(
//                    layerId = layerID
//                )
//                pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotaionConfig)!!
//            }
//        )
////    ----------------------------------------------------------------------------------------------
////    TODO: personal code
////    ----------------------------------------------------------------------------------------------
//        mapView.getMapboxMap().addOnCameraChangeListener() {
//            val currentZoom = mapView.getMapboxMap().cameraState.zoom
//            if(currentZoom<10 && annotationAdded){
////                Toast.makeText(this, "$currentZoom", Toast.LENGTH_SHORT).show()
//                pointAnnotationManager?.deleteAll()
//                annotationAdded=false
//            }
//            else if(currentZoom>=10 && markerList.isNotEmpty() && !annotationAdded){
//                pointAnnotationManager?.create(markerList)
//                annotationAdded=true
//            }
//            else if(currentZoom>=10 && !markerList.isNotEmpty() && buttonPressed ){
//                createMarkerList();
//            }
//        }
//    }
//    ----------------------------------------------------------------------------------------------
    //ToDo: Code for User Location : try converting to a class
//    ----------------------------------------------------------------------------------------------
//    private fun setupGesturesListener() {
//        mapView.gestures.addOnMoveListener(onMoveListener)
//    }
//
//    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
//    }
//
//    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
//        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
//        currentLatitude=it.latitude()
//        currentLongitude=it.longitude()
////        Toast.makeText(this, "location : "+currentLatitude+" -- "+currentLongitude, Toast.LENGTH_SHORT).show()
//    }
//    private val currentLocation = OnIndicatorPositionChangedListener{
//        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
//        currentLatitude=it.latitude()
//        currentLongitude=it.longitude()
//    }

//    private val onMoveListener = object : OnMoveListener {
//        override fun onMoveBegin(detector: MoveGestureDetector) {
//            onCameraTrackingDismissed()
//        }
//
//        override fun onMove(detector: MoveGestureDetector): Boolean {
//            return false
//        }
//
//        override fun onMoveEnd(detector: MoveGestureDetector) {}
//    }


//    private fun onCameraTrackingDismissed() {
////        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
//        mapView.location
//            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        mapView.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//        mapView.gestures.removeOnMoveListener(onMoveListener)
//    }
//
//    private fun initLocationComponent() {
//        val locationComponentPlugin = mapView.location
//        locationComponentPlugin.updateSettings {
//            this.enabled = true
////            ToDO: custom avatar puck in future (DO NOT DELETE)
////            this.locationPuck = LocationPuck2D(
////                bearingImage = AppCompatResources.getDrawable(
////                    this@MainActivity,
////                    R.drawable.iconsneakers,
////                ),
////                shadowImage = AppCompatResources.getDrawable(
////                    this@LocationTrackingActivity,
////                    R.drawable.mapbox_user_icon_shadow,
////                ),
////                scaleExpression = interpolate {
////                    linear()
////                    zoom()
////                    stop {
////                        literal(0.0)
////                        literal(0.6)
////                    }
////                    stop {
////                        literal(20.0)
////                        literal(1.0)
////                    }
////                }.toJson()
////            )
//        }
//        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//        mapView.location
//            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        mapView.gestures.removeOnMoveListener(onMoveListener)
//    }
//    ----------------------------------------------------------------------------------------------
//    ToDO: Step counter code for MVP: try converting to a class later
//    ----------------------------------------------------------------------------------------------


    override fun onResume() {
        super.onResume()
        running = true

        // TYPE_STEP_COUNTER:  A constant describing a step counter sensor
        // Returns the number of steps taken by the user since the last reboot while activated
        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // show toast message, if there is no sensor in the device
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // register listener with sensorManager
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    override fun onSensorChanged(event: SensorEvent?) {

        if (running) {

            //get the number of steps taken by the user.
            totalSteps = event!!.values[0]

            var currentSteps = totalSteps.toInt()

            val myEdit = pref.edit()
            if(initialSteps==-1){
                myEdit.putInt("initialSteps",currentSteps)
                initialSteps=currentSteps
            }
//            else if(currentSteps<initialSteps || currentSteps<disobeySteps){
//                disobeySteps+=currentSteps
//                initialSteps=currentSteps
//            }
            else{
                dailySteps+=currentSteps-initialSteps
                disobeySteps+=currentSteps-initialSteps
                initialSteps=currentSteps
            }
            myEdit.putInt("disobeySteps",disobeySteps)
            myEdit.putInt("dailySteps",dailySteps)
            myEdit.putInt("initialSteps",initialSteps)
//            if(disobeySteps>=100 && !tryOn.isEnabled){
////                Toast.makeText(this, "hurray", Toast.LENGTH_SHORT).show()
////                tryOn.isClickable=true
////                tryOn.alpha=1.0f
//                var dialog=Dialog(this)
//                dialog.setContentView(R.layout.hurray)
//                dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
//                dialog.show()
//                dialog.findViewById<ImageButton>(R.id.close).setOnClickListener {
//                    dialog.dismiss()
//                }
//                tryOn.isEnabled=true
//
//
//            }

            // set current steps in textview
            stepsTaken.text = ("$dailySteps")
            coins=dailySteps/100
            coinsEarned.text = ("${coins}")
            myEdit.commit()
        }
    }
//
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Implemented not required")
    }
//    ----------------------------------------------------------------------------------------------
//    ----------------------------------------------------------------------------------------------
//todo: unused function
//private fun addAnnotationToMap() {
//// Create an instance of the Annotation API and get the PointAnnotationManager.
//    bitmapFromDrawableRes(
//        this@MainActivity,
//        R.drawable.iconsneakers
//    )?.let {
//        val annotationApi = mapView?.annotations
//        val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
//// Set options for the resulting symbol layer.
//        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//// Define a geographic coordinate.
//            .withPoint(Point.fromLngLat( 75.9235,26.936))
////            .withPoint(Point.fromLngLat( 75.9235,25.936))
//// Specify the bitmap you assigned to the point annotation
//// The bitmap will be added to map style automatically.
//            .withIconImage(it)
//// Add the resulting pointAnnotation to the map.
//        pointAnnotationManager?.create(pointAnnotationOptions)
//    }
//}

//    private fun createLatLongForMarker(){
//        latitudeList.clear()
//        longitudeList.clear()
//        longitudeList.add(currentLongitude+0.0004)
//        latitudeList.add(currentLatitude+0.0004)
//        for (i in 0 until  21){
//            val latAdder=(-400..400).random()
//            val latMultiplyer=(-200..200).random()
//            latitudeList.add(currentLatitude+((latAdder*0.00001)*latMultiplyer*0.01))
//            val longAdder=(-400..400).random()
//            val longMultiplyer=(-200..200).random()
//            longitudeList.add(currentLongitude+((longAdder*0.00001)*longMultiplyer*0.01))
//        }
//        longitudeList.add(currentLongitude-0.0004)
//        latitudeList.add(currentLatitude-0.0004)
//        createMarkerList()
//    }
//    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
//        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))
//
//    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
//        if (sourceDrawable == null) {
//            return null
//        }
//        return if (sourceDrawable is BitmapDrawable) {
//            sourceDrawable.bitmap
//        } else {
//// copying drawable object to not manipulate on the same reference
//            val constantState = sourceDrawable.constantState ?: return null
//            val drawable = constantState.newDrawable().mutate()
//            val bitmap: Bitmap = Bitmap.createBitmap(
//                drawable.intrinsicWidth, drawable.intrinsicHeight,
//                Bitmap.Config.ARGB_8888
//            )
//            val canvas = Canvas(bitmap)
//            drawable.setBounds(0, 0, 10, 10)
//            drawable.draw(canvas)
//            bitmap
//        }
//    }
//    private fun createMarkerList(){
//
//        clearAnnotation();
//        markerList.clear()
//        specialMarkerList.clear()
//
//
//        // It will work when we create marker
//        pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation: PointAnnotation ->
//            var pointerLatitude=annotation.point.latitude()
//            var pointerLongitude=annotation.point.longitude()
////            initLocationComponent()
//
////            distance check
////            Toast.makeText(this,"dist "+(abs(pointerLatitude-currentLatitude)*100000)%1000+"\n"+(abs(currentLongitude-pointerLongitude)*100000)%1000,  Toast.LENGTH_SHORT).show()
//            if(abs(pointerLatitude-currentLatitude)<=0.0005 && abs(currentLongitude-pointerLongitude)<=0.0005){
//                onMarkerItemClick(annotation)
//            }
//            else{
//                Toast.makeText(this,"get closer to interact", Toast.LENGTH_SHORT).show()
//            }
//            true
//        })
//        markerList =  ArrayList();
//        specialMarkerList =  ArrayList();
//        var bitmpa = convertDrawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.simple_marker))
//        for (i in 0 until  21){
//
//            var mObe = JSONObject();
//            mObe.put("somekey",i);
//            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//                .withPoint(Point.fromLngLat(longitudeList.get(i),latitudeList.get(i)))
//                .withData(Gson().fromJson(mObe.toString(), JsonElement::class.java))
//                .withIconImage(bitmpa!!)
//                .withIconSize(0.2)
//            markerList.add(pointAnnotationOptions);
//        }
//
//        bitmpa = convertDrawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.ar_marker))
//        for (i in 21 until 23){
//
//            var mObe = JSONObject();
//            mObe.put("somekey",i);
//            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//                .withPoint(Point.fromLngLat(longitudeList.get(i),latitudeList.get(i)))
//                .withData(Gson().fromJson(mObe.toString(), JsonElement::class.java))
//                .withIconImage(bitmpa!!)
//                .withIconSize(0.2)
////            need SDF image for halo links
////            https://github.com/mapbox/mapbox-plugins-android/issues/868
////            https://stackoverflow.com/questions/63299999/how-can-i-create-sdf-icons-used-in-mapbox-from-png
////                .withIconHaloWidth(5.0)
////                .withIconHaloColor(Color.RED)
////                .withIconHaloBlur(1.0)
//            specialMarkerList.add(pointAnnotationOptions);
//        }
//        pointAnnotationManager?.create(markerList)
//        pointAnnotationManager?.create(specialMarkerList)
//        annotationAdded=true
//    }
//
//    fun clearAnnotation(){
//        markerList = ArrayList();
//        pointAnnotationManager?.deleteAll()
//    }

//    private fun onMarkerItemClick(marker: PointAnnotation) {
//        countnum++;
//        var number= Integer.parseInt(marker.getData()?.asJsonObject?.get("somekey").toString())
////        AlertDialog.Builder(this)
////            .setTitle("Marker Click")
////            .setMessage("Here is the value-- "+number)
////            .setPositiveButton(
////                "OK"
////            ) { dialog, whichButton ->
////                dialog.dismiss()
////            }
////            .setNegativeButton(
////                "Cancel"
////            ) { dialog, which -> dialog.dismiss() }.show()
//        var dialog=Dialog(this)
//        dialog.setContentView(R.layout.hurray)
//        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
//        dialog.findViewById<ImageButton>(R.id.close).setOnClickListener {
//            dialog.dismiss()
//        }
//        var animationWindow=dialog.findViewById<LottieAnimationView>(R.id.animationView)
//        var imageWindow=dialog.findViewById<ImageView>(R.id.reward)
//        if(timeoutSet.contains(number)){
//            dialog.findViewById<TextView>(R.id.t1).text = "You've already used up this stash"
//            dialog.findViewById<TextView>(R.id.t2).visibility=View.GONE
//            animationWindow.visibility=View.GONE
//        }
//        else {
//            timeoutSet.add(number)
//            if (number > 20) {
////            Toast.makeText(this,"yeah",Toast.LENGTH_SHORT).show()
//                dialog.findViewById<TextView>(R.id.t1).text = "You've found out our 3D try-ons"
//                dialog.findViewById<TextView>(R.id.t2).text = "Search around and try em on"
//                animationWindow.setRepeatCount(LottieDrawable.INFINITE)
//                dialog.findViewById<Button>(R.id.find).visibility = View.VISIBLE
//                dialog.findViewById<Button>(R.id.find).setOnClickListener {
//                    val intent = Intent(this, snapCam::class.java)
//                    intent.putExtra("Type", "2")
//                    startActivity(intent)
//                }
////            dialog.findViewById<TextView>(R.id.t2).text="Search around and try em on"
//            } else {
//                animationWindow.addAnimatorListener(object : Animator.AnimatorListener {
//                    override fun onAnimationStart(animation: Animator) {
//                    }
//
//                    override fun onAnimationEnd(animation: Animator) {
//                        //Your code for remove the fragment
////                try {
//
//                        imageWindow.visibility = View.VISIBLE
//                        val selectShoe = (0..100).random()
////                    one legendary
//                        if (selectShoe >= 10 && selectShoe <= 15) {
//                            imageWindow.setImageResource(one)
//                            dialog.findViewById<TextView>(R.id.t2).text =
//                                "Finally a Legendary sneaker!!\n those are only 5 in a 100"
//                        } else if (selectShoe >= 20 && selectShoe <= 30) {
//                            imageWindow.setImageResource(two)
//                            dialog.findViewById<TextView>(R.id.t2).text =
//                                "Wohoo!! a rare collectible\n Let's cop a Legendary next"
//                        } else {
//                            dialog.findViewById<TextView>(R.id.t2).text =
//                                "You got a common sneaker, keep playing for rare ones"
//                        }
////                } catch (ex: Exception) {
////                    ex.toString()
////                }
////                println("yeahs")
////                Toast.makeText(this@MainActivity,"yeah", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onAnimationCancel(animation: Animator) {
//                    }
//
//                    override fun onAnimationRepeat(animation: Animator) {
//                    }
//                })
//            }
//        }
//        dialog.show()
//    }
//    ----------------------------------------------------------------------------------------------
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
}

