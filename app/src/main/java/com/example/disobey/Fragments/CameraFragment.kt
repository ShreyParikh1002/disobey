package com.example.disobey.Fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import com.example.disobey.R
import com.snap.camerakit.support.app.CameraActivity
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {

    lateinit var captureResultLabel : TextView
    lateinit var imageView : ImageView
    lateinit var videoView : VideoView
    lateinit var bitmap: Bitmap
    lateinit var uri: Uri
    var typ =".jpg"
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val type = Integer.parseInt(intent.getStringExtra("Type"))
        val type=1;
        var typeLensId="6e3bf43c-bda9-48e1-8be6-0dc2a936bc67"
        captureResultLabel = view.findViewById<TextView>(R.id.label_capture_result)
        imageView = view.findViewById<ImageView>(R.id.image_preview)
        videoView = view.findViewById<VideoView>(R.id.video_preview).apply {
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
            }
        }
        if(type==1){
            typeLensId="009495ba-d39f-4ae3-857d-8c1c7f0a4add"
        }
        captureLauncher.launch(
            CameraActivity.Configuration.WithLens(
                lensGroupId = "81f7449b-3a9d-415a-af34-bf2b3cd93a42",
                lensId = typeLensId,
                cameraFacingFront = false
            )
        )
        view.findViewById<Button>(R.id.button_capture_lens).setOnClickListener {
            captureLauncher.launch(
                CameraActivity.Configuration.WithLens(
                    lensGroupId = "81f7449b-3a9d-415a-af34-bf2b3cd93a42",
                    lensId = typeLensId,
                    cameraFacingFront = false
                )
            )
        }
        view.findViewById<Button>(R.id.download).setOnClickListener{
//            if(::uri.isInitialized
//            ){
////                val inputStream = contentResolver.openInputStream(uri)
////                val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//////                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
////                val fileName = "DisobeyImage_"+typ
//////                println(mimeType+ fileName)
////                val file = File(outputDir, fileName)
////                val outputStream = FileOutputStream(file)
////                val buffer = ByteArray(1024)
////                var length = 0
////                while ((inputStream!!.read(buffer).also { length = it }) > 0) {
////                    outputStream.write(buffer, 0, length)
////                }
////                outputStream.flush()
////                outputStream.close()
////                inputStream.close()
//                val inputStream = contentResolver.openInputStream(uri)
//                val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val fileName = "meow.jpg"
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
//            }
//            else{
//                Toast.makeText(context, "Please take an image/video first", Toast.LENGTH_SHORT).show()
//            }

        }
    }
    val clearMediaPreviews = {
        videoView.visibility = View.GONE
        imageView.visibility = View.GONE
    }
    val captureLauncher = (requireActivity() as ComponentActivity).registerForActivityResult(CameraActivity.Capture) { result ->
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}