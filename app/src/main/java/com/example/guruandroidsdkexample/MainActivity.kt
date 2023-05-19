package com.example.guruandroidsdkexample

import ai.getguru.androidsdk.GuruVideo
import ai.getguru.androidsdk.GuruVideoImpl
import ai.getguru.androidsdk.PoseTracker
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), CoroutineScope {

    private val LOG_TAG = "MainActivity"

    private var guruVideo: GuruVideo? = null
    private var poseTracker: PoseTracker? = null
    private lateinit var loadGuruJob: Job
    private val fpsGauge = FpsGauge()
    private var overlaysView: SkeletonOverlayView? = null
    private val API_KEY = "YOUR_API_KEY_HERE"

    // Set to true if you want an ai.getguru.androidsdk.InferenceResult, which includes analysis
    // Set to false if all you want is ai.getguru.androidsdk.Keypoints
    private val sendToGuruForAnalysis: Boolean = true

    override val coroutineContext
        get() = Dispatchers.IO + loadGuruJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        overlaysView = findViewById(R.id.result_preview)
        loadGuruJob = Job()
        launch(loadGuruJob) {
            if (sendToGuruForAnalysis) {
                guruVideo = GuruVideoImpl.create(
                    "calisthenics",
                    "bodyweight_squat",
                    API_KEY,
                    applicationContext,
                )
            } else {
                poseTracker = PoseTracker.createWithModel(
                    API_KEY,
                    applicationContext,
                    "posemodel.onnx",
                    assets,
                )
            }

            runOnUiThread {
                captureCameraFragment()
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private suspend fun onNewImage(img: ImageProxy) {
        if (guruVideo == null && poseTracker == null) {
            return
        }
        if (img.image == null) {
            return
        }

        if (sendToGuruForAnalysis) {
            val results = guruVideo!!.newFrame(img.image!!, img.imageInfo.rotationDegrees)
            overlaysView?.imageAspectRatio = img.height.toFloat()/ img.width.toFloat()
            overlaysView?.analysis = results?.analysis
            overlaysView?.keypoints = results?.skeleton()
        } else {
            overlaysView?.imageAspectRatio = img.height.toFloat()/ img.width.toFloat()
            overlaysView?.keypoints = poseTracker!!.newFrame(img.image!!, img.imageInfo.rotationDegrees)
        }
        fpsGauge.onFrameFinish()
        overlaysView?.setCurrentFps(fpsGauge.currentFps())
    }


    override fun onDestroy() {
        super.onDestroy()
        loadGuruJob.cancel()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun captureCameraFragment() {
        val fragment = CameraSetupFragment(object: CameraSetupFragment.ImageConsumer {
            override fun onImageAvailable(img: ImageProxy) {
                runBlocking {
                    onNewImage(img)
                }
            }
        })
        supportFragmentManager.beginTransaction().replace(R.id.preview_view, fragment).commitAllowingStateLoss()
    }
}
