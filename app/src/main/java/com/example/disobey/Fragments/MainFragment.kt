package com.example.disobey.Fragments

import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getSystemService
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.disobey.R
import com.example.disobey.SneakerData
import com.example.disobey.SneakerDataStruc
import com.example.disobey.snapCam
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.expressions.dsl.generated.image
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearingSource
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
import com.mapbox.maps.plugin.locationcomponent.location2
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(), SensorEventListener {
//    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    data class Coordinate(val latitude: Double, val longitude: Double)

    var coordinateList: MutableList<Coordinate> = mutableListOf()

    lateinit var v: View
    private lateinit var mapView: MapView
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
    //    ----------------------------------------------------------------------------------------------
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor?=null
    private var running = false
    private var totalSteps = 0f
    lateinit var cameraButton: ImageButton
    lateinit var recenterButton: ImageButton
    lateinit var avatarButton: ImageButton
    lateinit var scan: Button
    lateinit var stepsTaken : TextView
    lateinit var coinsEarned : TextView

    lateinit var pref: SharedPreferences
    private var initialSteps = 0
    private var disobeySteps = 0
    private var dailySteps = 0
    private var coins = 0
    val timeoutSet = mutableSetOf(24)

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

    lateinit var sneakerList :ArrayList<SneakerDataStruc>
    var one=R.drawable.loneshark
    var two=R.drawable.darksmoke
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_main, container, false)

        stepsTaken = v.findViewById(R.id.stepCount)
        coinsEarned = v.findViewById(R.id.coinCount)
//        cameraButton=v.findViewById(R.id.cameraButton)
//        avatarButton=v.findViewById(R.id.avatar)

//--------------------------------------------------------------------------------------------------
        mapView = v.findViewById(R.id.mapView)
        onMapReady()
        mapView.location.addOnIndicatorPositionChangedListener(currentLocation)

        recenterButton=v.findViewById(R.id.recenterButton)

        recenterButton.setOnClickListener {
            mapView.getMapboxMap().flyTo(
                CameraOptions.Builder()
                    .zoom(17.0)
                    .build()
            )
            initLocationComponent()
            setupGesturesListener()

//                    val intent = Intent(context, snapCam::class.java)
//                    intent.putExtra("Type", "2")
//                    startActivity(intent)
//
        }

        scan=v.findViewById(R.id.scan)
        scan.setOnClickListener {
            val currentDate = LocalDate.now().toString()
            val storedDate = pref.getString("storedDate", null)
            if(storedDate!=currentDate){
                createLatLongForMarker()
                timeoutSet.clear()
                buttonPressed=true
                val editor = pref.edit()
                editor.putString("storedDate", currentDate)
                editor.apply()
            }
            else{
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle("Try Again Tomorrow")
                alertDialogBuilder.setMessage("You have already scanned this area today. Please try again tomorrow or visit some area outside this 1Km radius.")
                alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

//--------------------------------------------------------------------------------------------------
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        pref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        initialSteps= pref.getInt("initialSteps",-1)
        disobeySteps= pref.getInt("disobeySteps",0)
        dailySteps= pref.getInt("dailySteps",0)
//        if(disobeySteps>100){
//            tryOn.isEnabled=true
//        }
        stepsTaken.text = ("${dailySteps}")
        coins=dailySteps/100
        coinsEarned.text = ("${coins}")

        val coordinateListString = pref.getString("coordinateList", null)
        if (coordinateListString != null) {
            try {
                val gson = Gson()
                val type = object : TypeToken<MutableList<Coordinate>>() {}.type
                coordinateList = gson.fromJson(coordinateListString, type)
                println(coordinateList)
                createMarkerList()
                // Use the retrieved coordinateList as needed
            } catch (e: JsonSyntaxException) {
                // Handle JSON deserialization error
                e.printStackTrace()
            }
        } else {
            // User hasn't scanned yet
        }

        return v;
    }
    private fun onMapReady() {

        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(17.0)
                .build()
        )
        mapView.getMapboxMap().loadStyle(
            style(styleUri="mapbox://styles/shrey1002/clhre4h8x01z101pgaekh31wy/draft")
            {
                initLocationComponent()
                setupGesturesListener()
                annotationApi = mapView?.annotations
                annotaionConfig = AnnotationConfig(
                    layerId = layerID
                )
                pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotaionConfig)!!
            }
        )
//    ----------------------------------------------------------------------------------------------
//    TODO: personal code
//    ----------------------------------------------------------------------------------------------
        mapView.getMapboxMap().addOnCameraChangeListener() {
            val currentZoom = mapView.getMapboxMap().cameraState.zoom
            if(currentZoom<10 && annotationAdded){
//                Toast.makeText(this, "$currentZoom", Toast.LENGTH_SHORT).show()
                pointAnnotationManager?.deleteAll()
                pointAnnotationManager?.deleteAll()
                annotationAdded=false
            }
            else if(currentZoom>=10 && markerList.isNotEmpty() && !annotationAdded){
                pointAnnotationManager?.create(markerList)
                annotationAdded=true
            }
            else if(currentZoom>=10 && !markerList.isNotEmpty() && buttonPressed ){
                createMarkerList();
            }
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location2
        locationComponentPlugin.apply {
            this.enabled = true
//            this.puckBearingSource=PuckBearingSource.HEADING
            this.pulsingEnabled=true
            this.pulsingMaxRadius= 100.0F
//            this.pulsingColor=R.color.black
            this.locationPuck = LocationPuck2D(
//                bearingImage = context?.let {
//                    AppCompatResources.getDrawable(
//                        it,
//                        R.drawable.iconsneakers,
//                    )
//                },
                topImage = context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.disobeyavatar2d,
                    )
                },
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.0)
                    }
                    stop {
                        literal(20.0)
                        literal(0.2)
                    }
                }.toJson()
            )

        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        currentLatitude=it.latitude()
        currentLongitude=it.longitude()
//        Toast.makeText(this, "location : "+currentLatitude+" -- "+currentLongitude, Toast.LENGTH_SHORT).show()
    }

    private val currentLocation = OnIndicatorPositionChangedListener{
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        currentLatitude=it.latitude()
        currentLongitude=it.longitude()
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun onCameraTrackingDismissed() {
//        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
        val locationComponentPlugin = mapView.location2
        locationComponentPlugin.apply {
            this.puckBearingEnabled = false
//            this.puckBearingSource=PuckBearingSource.HEADING
            this.pulsingEnabled=false
//            this.puckBearingSource=PuckBearingSource.HEADING
        }
    }

    private fun createLatLongForMarker(){
        latitudeList.clear()
        longitudeList.clear()
        coordinateList.clear()
        longitudeList.add(currentLongitude+0.0004)
        latitudeList.add(currentLatitude+0.0004)
        val radius = 1000.0 // 2km radius
        val numberOfPoints = 49

        val centerLatitude = currentLatitude
        val centerLongitude = currentLongitude

        val random = Random.Default

        for (i in 0 until numberOfPoints) {
            val angle = random.nextDouble(0.0, 2 * Math.PI)
            val distance = random.nextDouble(0.0, radius)

            val latitudeOffset = distance * sin(angle) / 110574.0 // Convert to degrees
            val longitudeOffset = distance * cos(angle) / (111320.0 * cos(centerLatitude * Math.PI / 180.0)) // Convert to degrees

            val latitude = centerLatitude + latitudeOffset
            val longitude = centerLongitude + longitudeOffset

            coordinateList.add(Coordinate(latitude, longitude))
        }
        coordinateList.add(Coordinate((currentLatitude+0.0004),(currentLongitude+0.0004)))
        coordinateList.sortBy { ((it.latitude-currentLatitude)*(it.latitude-currentLatitude)) + ((it.longitude-currentLongitude)*(it.longitude-currentLongitude))}
//        for (i in 0 until  49   ){
//            val number=(((coordinateList[i].latitude-currentLatitude)*(coordinateList[i].latitude-currentLatitude)) + ((coordinateList[i].longitude-currentLongitude)*(coordinateList[i].longitude-currentLongitude)))
//            val formattedNumber = String.format("%.8f", number)
//            println(formattedNumber)
//        }
        val editor = pref.edit()
        val gson = Gson()
        val coordinateListAsString = gson.toJson(coordinateList)
        editor.putString("coordinateList", coordinateListAsString)
        editor.apply()
        createMarkerList()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun createMarkerList(){

        clearAnnotation();
        markerList.clear()
        specialMarkerList.clear()


        // It will work when we create marker
        pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation: PointAnnotation ->
            var pointerLatitude=annotation.point.latitude()
            var pointerLongitude=annotation.point.longitude()
//            initLocationComponent()

//            distance check
//            Toast.makeText(this,"dist "+(abs(pointerLatitude-currentLatitude)*100000)%1000+"\n"+(abs(currentLongitude-pointerLongitude)*100000)%1000,  Toast.LENGTH_SHORT).show()
            if(abs(pointerLatitude-currentLatitude) <=0.0005 && abs(currentLongitude-pointerLongitude) <=0.0005){
                onMarkerItemClick(annotation)
            }
            else{
                Toast.makeText(context,"get closer to interact", Toast.LENGTH_SHORT).show()
            }
            true
        })
        markerList =  ArrayList();
        specialMarkerList =  ArrayList();
        var stashIcon = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.simple_marker))
        for (i in 0 until  50){
            if(i<=36){
                stashIcon = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.a))
            }
            else if(i<=44){
                stashIcon = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.b))
            }
            else if(i<=48){
                stashIcon = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.c))
            }
            else if(i==49){
                stashIcon = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.d))
            }
            var keyJsonObject = JSONObject();
            keyJsonObject.put("key",i);
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(coordinateList.get(i).longitude,coordinateList.get(i).latitude))
                .withData(Gson().fromJson(keyJsonObject.toString(), JsonElement::class.java))
                .withIconImage(stashIcon!!)
                .withIconSize(0.8)
            markerList.add(pointAnnotationOptions);
        }
        val msneaker=SneakerData()
        sneakerList=msneaker.populateMarkers()

//        TODO: golden box part
//        bitmpa = convertDrawableToBitmap(AppCompatResources.getDrawable(requireContext(), R.drawable.ar_marker))
//        for (i in 21 until 23){
//
//            var mObe = JSONObject();

//            mObe.put("key",i);
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
        pointAnnotationManager?.create(markerList)
        pointAnnotationManager?.create(specialMarkerList)
        annotationAdded=true
    }

    fun clearAnnotation(){
        markerList = ArrayList();
        pointAnnotationManager?.deleteAll()
    }

    private fun onMarkerItemClick(marker: PointAnnotation) {
        countnum++;
        var number= Integer.parseInt(marker.getData()?.asJsonObject?.get("key").toString())
//        AlertDialog.Builder(this)
//            .setTitle("Marker Click")
//            .setMessage("Here is the value-- "+number)
//            .setPositiveButton(
//                "OK"
//            ) { dialog, whichButton ->
//                dialog.dismiss()
//            }
//            .setNegativeButton(
//                "Cancel"
//            ) { dialog, which -> dialog.dismiss() }.show()
        var dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.hurray)
        dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.findViewById<ImageButton>(R.id.close).setOnClickListener {
            dialog.dismiss()
        }
        var animationWindow=dialog.findViewById<LottieAnimationView>(R.id.animationView)
        var imageWindow=dialog.findViewById<ImageView>(R.id.reward)
        if(timeoutSet.contains(number)){
            dialog.findViewById<TextView>(R.id.t1).text = "You've already used up this stash"
            dialog.findViewById<TextView>(R.id.t2).visibility=View.GONE
            animationWindow.visibility=View.GONE
        }
        else {
            timeoutSet.add(number)
//            TODO:legacy code for golden stashes
            if (number > 20) {
//            Toast.makeText(this,"yeah",Toast.LENGTH_SHORT).show()
                dialog.findViewById<TextView>(R.id.t1).text = "You've found out our 3D try-ons"
                dialog.findViewById<TextView>(R.id.t2).text = "Search around and try em on"
                animationWindow.setRepeatCount(LottieDrawable.INFINITE)
                dialog.findViewById<Button>(R.id.find).visibility = View.VISIBLE
                dialog.findViewById<Button>(R.id.find).setOnClickListener {
                    val intent = Intent(context, snapCam::class.java)
                    intent.putExtra("Type", "2")
                    startActivity(intent)
                }
//            dialog.findViewById<TextView>(R.id.t2).text="Search around and try em on"
            } else {
                animationWindow.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        //Your code for remove the fragment
//                try {

                        imageWindow.visibility = View.VISIBLE
                        val selectShoe = (0..100).random()
//                    one legendary
                        if (selectShoe >= 10 && selectShoe <= 15) {
                            imageWindow.setImageResource(one)
                            dialog.findViewById<TextView>(R.id.t2).text =
                                "Finally a Legendary sneaker!!\n those are only 5 in a 100"
                        } else if (selectShoe >= 20 && selectShoe <= 30) {
                            imageWindow.setImageResource(two)
                            dialog.findViewById<TextView>(R.id.t2).text =
                                "Wohoo!! a rare collectible\n Let's cop a Legendary next"
                        } else {
                            dialog.findViewById<TextView>(R.id.t2).text =
                                "You got a common sneaker, keep playing for rare ones"
                        }
//                } catch (ex: Exception) {
//                    ex.toString()
//                }
//                println("yeahs")
//                Toast.makeText(this@MainActivity,"yeah", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
            }
        }
        dialog.show()
    }

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, 10, 10)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        println("runnong true")
        // TYPE_STEP_COUNTER:  A constant describing a step counter sensor
        // Returns the number of steps taken by the user since the last reboot while activated
        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // show toast message, if there is no sensor in the device
//            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // register listener with sensorManager
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    override fun onSensorChanged(event: SensorEvent?) {

        if (running) {
            println( "onSensorChanged called")
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

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment MainFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            MainFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}