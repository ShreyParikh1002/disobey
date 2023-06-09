package com.example.disobey

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.snap.camerakit.support.app.CameraActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class snapCam : AppCompatActivity() {

    lateinit var captureResultLabel :TextView
    lateinit var imageView : ImageView
    lateinit var videoView :VideoView
    lateinit var bitmap: Bitmap
    lateinit var uri: Uri
    var typ =".jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snap_cam)

        val type = Integer.parseInt(intent.getStringExtra("Type"))
        var typeLensId="6e3bf43c-bda9-48e1-8be6-0dc2a936bc67"
        captureResultLabel = findViewById<TextView>(R.id.label_capture_result)
        imageView = findViewById<ImageView>(R.id.image_preview)
        videoView = findViewById<VideoView>(R.id.video_preview).apply {
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
            }
        }
        if(type==1){
            typeLensId="d36f63db-23f1-457b-8059-3914de823243"
        }
        captureLauncher.launch(
            CameraActivity.Configuration.WithLens(
                lensGroupId = "81f7449b-3a9d-415a-af34-bf2b3cd93a42",
                lensId = typeLensId,
                cameraFacingFront = false
            )
        )
        findViewById<Button>(R.id.button_capture_lens).setOnClickListener {
            captureLauncher.launch(
                CameraActivity.Configuration.WithLens(
                    lensGroupId = "81f7449b-3a9d-415a-af34-bf2b3cd93a42",
                    lensId = typeLensId,
                    cameraFacingFront = false
                )
            )
        }
        findViewById<Button>(R.id.download).setOnClickListener{
            if(::uri.isInitialized
            ){
//                val inputStream = contentResolver.openInputStream(uri)
//                val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
////                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val fileName = "DisobeyImage_"+typ
////                println(mimeType+ fileName)
//                val file = File(outputDir, fileName)
//                val outputStream = FileOutputStream(file)
//                val buffer = ByteArray(1024)
//                var length = 0
//                while ((inputStream!!.read(buffer).also { length = it }) > 0) {
//                    outputStream.write(buffer, 0, length)
//                }
//                outputStream.flush()
//                outputStream.close()
//                inputStream.close()
                val inputStream = contentResolver.openInputStream(uri)
                val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "meow.jpg"
                val file = File(outputDir, fileName)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var length = 0
                while ((inputStream!!.read(buffer).also { length = it }) > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()


            }
            else{
                Toast.makeText(this, "Please take an image/video first", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun download() {
        TODO("Not yet implemented")

    }

    val clearMediaPreviews = {
        videoView.visibility = View.GONE
        imageView.visibility = View.GONE
    }
    val captureLauncher = (this as ComponentActivity).registerForActivityResult(CameraActivity.Capture) { result ->
        Log.d(ContentValues.TAG, "Got capture result: $result")
        when (result) {
            is CameraActivity.Capture.Result.Success.Video -> {
                videoView.visibility = View.VISIBLE
                videoView.setVideoURI(result.uri)
//                Toast.makeText(this, "${result.uri}", Toast.LENGTH_SHORT).show()
                videoView.start()
                imageView.visibility = View.GONE
                captureResultLabel.text = null
                uri = result.uri
                typ=".mp4"

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    try {
//                        val source = ImageDecoder.createSource(this.contentResolver, result.uri)
//                        bitmap = ImageDecoder.decodeBitmap(source)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                else{
//                    bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver, result.uri)
//                }
            }
            is CameraActivity.Capture.Result.Success.Image -> {
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(result.uri)
//                Toast.makeText(this, "${result.uri}", Toast.LENGTH_SHORT).show()
                videoView.visibility = View.GONE
                captureResultLabel.text = null
                uri = result.uri
                typ=".jpg"
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    try {
//                        val source = ImageDecoder.createSource(this.contentResolver, result.uri)
//                        bitmap = ImageDecoder.decodeBitmap(source)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                else{
//                    bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver, result.uri)
//                }
            }
            is CameraActivity.Capture.Result.Cancelled -> {
                captureResultLabel.text = getString(R.string.label_result_none)
                clearMediaPreviews()
            }
            is CameraActivity.Capture.Result.Failure -> {
                captureResultLabel.text = getString(
                    R.string.label_result_failure, result.exception.toString()
                )
                clearMediaPreviews()
            }
//                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri))
//            } else {
//                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
//            }

        }
    }
//    ----------------------------------------------------------------------------------------------
    fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            this?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//                    Primary directory Download not allowed for content://media/external/images/media; allowed directories are [DCIM, Pictures]
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                //INTERNAL_CONTENT_URI is to store to internal storage denied
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "downloaded", Toast.LENGTH_SHORT).show()
        }
    }
//    ----------------------------------------------------------------------------------------------
}