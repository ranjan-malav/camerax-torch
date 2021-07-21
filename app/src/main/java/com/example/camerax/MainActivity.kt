package com.example.camerax

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cam: Camera? = null
    private var isFlashOn = false
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var luminosityAnalyzer = LuminosityAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val torch = findViewById<MaterialButton>(R.id.torch)
        torch.setOnClickListener {
            if (isFlashOn) {
                cam?.let { switchFlashOff(it) }
            } else {
                cam?.let { switchFlashOn(it) }
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, luminosityAnalyzer) }
                cam = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )
            } catch (exc: Exception) {
                //Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchFlashOn(cam: Camera) {
        if (cam.cameraInfo.hasFlashUnit()) {
            //Log.d(TAG, "Switch flash on")
            cam.cameraControl.enableTorch(true)
            isFlashOn = true
        }
    }

    private fun switchFlashOff(cam: Camera) {
        if (cam.cameraInfo.hasFlashUnit()) {
            //Log.d(TAG, "Switch flash off")
            cam.cameraControl.enableTorch(false)
            isFlashOn = false
        }
    }

}

class LuminosityAnalyzer : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        image.close()
    }
}