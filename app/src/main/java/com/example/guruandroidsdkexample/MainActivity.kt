package com.example.guruandroidsdkexample

import ai.getguru.androidsdk.GuruVideo
import ai.getguru.androidsdk.GuruVideoImpl
import ai.getguru.androidsdk.InferenceLandmark
import ai.getguru.androidsdk.Keypoint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener, CoroutineScope {

    private val LOG_TAG = "MainActivity"
    private var guruVideo: GuruVideo? = null
    private val imageSize = Size(640, 480)
    private lateinit var loadGuruJob: Job
    private lateinit var overlayView: ImageView
    private lateinit var overlayCanvas: Canvas
    private lateinit var overlayPaint: Paint

    override val coroutineContext
        get() = Dispatchers.IO + loadGuruJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initOverlay()

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1122)
        } else {
            loadGuruJob = Job()
            launch(loadGuruJob) {
                guruVideo = GuruVideoImpl.create(
                    "calisthenics",
                    "bodyweight_squat",
                    "your-api-key",
                    applicationContext
                )

                runOnUiThread {
                    captureCameraFragment()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadGuruJob.cancel()
    }

    override fun onImageAvailable(reader: ImageReader) {
        runBlocking {
            reader.acquireLatestImage().use { image ->
                if (image != null && guruVideo != null) {
                    val inference = guruVideo!!.newFrame(image)

                    Log.i(LOG_TAG, "Counted ${inference.analysis.reps.size} ${inference.analysis.movement} reps so far")

                    runOnUiThread {
                        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.LEFT_WRIST),
                            inference.keypointForLandmark(InferenceLandmark.LEFT_ELBOW)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.LEFT_ELBOW),
                            inference.keypointForLandmark(InferenceLandmark.LEFT_SHOULDER)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.LEFT_SHOULDER),
                            inference.keypointForLandmark(InferenceLandmark.LEFT_HIP)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.LEFT_HIP),
                            inference.keypointForLandmark(InferenceLandmark.LEFT_KNEE)
                        )

                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_WRIST),
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_ELBOW)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_ELBOW),
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_SHOULDER)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_SHOULDER),
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_HIP)
                        )
                        drawJointTo(
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_HIP),
                            inference.keypointForLandmark(InferenceLandmark.RIGHT_KNEE)
                        )

                        overlayView.invalidate()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureCameraFragment()
        } else {
            finish()
        }
    }

    private fun captureCameraFragment() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String = (
                getFacingCameraId(manager, CameraCharacteristics.LENS_FACING_FRONT)
                    ?: getFacingCameraId(manager, CameraCharacteristics.LENS_FACING_BACK)
                )!!

        val camera2Fragment = CameraStreamFragment.newInstance(
            cameraId,
            this,
            R.layout.camera_fragment,
            imageSize
        )
        supportFragmentManager.beginTransaction().replace(R.id.container, camera2Fragment).commit()
    }

    private fun drawJointTo(from: Keypoint?, to: Keypoint?) {
        if (from != null) {
            overlayCanvas.drawCircle(
                (from.x * imageSize.width).toFloat(),
                (from.y * imageSize.height).toFloat(),
                15.0F,
                overlayPaint)

            if (to != null) {
                overlayCanvas.drawLine(
                    (from.x * imageSize.width).toFloat(),
                    (from.y * imageSize.height).toFloat(),
                    (to.x * imageSize.width).toFloat(),
                    (to.y * imageSize.height).toFloat(),
                    overlayPaint
                )
            }
        }
    }

    private fun getFacingCameraId(cameraManager: CameraManager, cameraDirection: Int): String? {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
            val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
            if (orientation == cameraDirection) {
                return cameraId
            }
        }
        return null
    }

    private fun initOverlay() {
        overlayView = findViewById(R.id.overlay)

        // Getting the current window dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val dw = displayMetrics.widthPixels
        val dh = displayMetrics.heightPixels

        // Creating a bitmap with fetched dimensions
        val bitmap = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888)

        // Storing the canvas on the bitmap
        overlayCanvas = Canvas(bitmap)

        // Initializing Paint to determine
        // stoke attributes like color and size
        overlayPaint = Paint()
        overlayPaint.color = Color.WHITE
        overlayPaint.strokeWidth = 10F

        // Setting the bitmap on ImageView
        overlayView.setImageBitmap(bitmap)
    }
}
