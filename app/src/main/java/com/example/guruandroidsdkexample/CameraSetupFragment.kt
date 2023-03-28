package com.example.guruandroidsdkexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraSetupFragment : Fragment {

    interface ImageConsumer {
        fun onImageAvailable(img: ImageProxy)
    }

    constructor(onImageAvailable: ImageConsumer) : super() {
        this.imageConsumer = onImageAvailable
    }

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var previewView: PreviewView? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val imageConsumer: ImageConsumer

    companion object {
        const val CAMERA_PERMISSION_CODE = 401
        val IMAGE_SIZE: Size = Size(480, 640)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = requireActivity().requireViewById(R.id.preview_view)
        ensurePermissionsAndStartCamera()
    }

    private fun ensurePermissionsAndStartCamera() {
        val activity = this.requireActivity()
        val permission =
            ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this.requireActivity(), arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture!!.addListener(Runnable {
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture!!.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults!!)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults == null || grantResults.isEmpty()) {
                throw RuntimeException("Grant result is empty!")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                throw RuntimeException("Camera permission denied!")
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        val imageAnalysis =
            ImageAnalysis.Builder()
                .setTargetResolution(IMAGE_SIZE)
                // YUV_420_888 is the default, but just to make it explicit...
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        imageAnalysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
            imageConsumer.onImageAvailable(imageProxy)
            imageProxy.close()
        }

        val preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(previewView!!.surfaceProvider)
        // FIT_CENTER looks better, but may add margins that you'll need to account for if
        // you paint landmarks over the image preview
        previewView!!.scaleType = PreviewView.ScaleType.FIT_START

        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }
}