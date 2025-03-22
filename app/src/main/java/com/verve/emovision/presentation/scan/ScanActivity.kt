package com.verve.emovision.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.verve.emovision.R
import com.verve.emovision.databinding.ActivityScanBinding
import com.verve.emovision.utils.FaceBox
import com.verve.emovision.utils.OverlayView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private val binding: ActivityScanBinding by lazy {
        ActivityScanBinding.inflate(layoutInflater)
    }
    private lateinit var overlayView: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private var tflite: Interpreter? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        overlayView = findViewById(R.id.overlayView)
        initTensorFlow()
        requestPermissions()
        initCameraExecutor()
    }

    private fun initTensorFlow() {
        try {
            val options = Interpreter.Options().apply { setUseXNNPACK(true) }
            tflite = Interpreter(loadModelFile(), options)
            Log.d("TFLite", "Model berhasil dimuat!")
        } catch (e: Exception) {
            Log.e("TFLite", "Gagal memuat model: ${e.localizedMessage}")
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startCamera()
        }
    }

    private fun initCameraExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer =
                ImageAnalysis.Builder().build().also {
//                    it.setTargetRotation(binding.previewView.display.rotation)
                    it.setAnalyzer(cameraExecutor) { imageProxy -> analyzeImage(imageProxy) }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val detector =
            FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .enableTracking()
                    .build(),
            )

        detector.process(image)
            .addOnSuccessListener { faces ->
                val faceBoxes = mutableListOf<FaceBox>()

                for (face in faces) {
                    val bitmap = imageProxy.toBitmap().flipHorizontal()
                    val faceBitmap = cropFace(bitmap, face)

                    if (faceBitmap != null && tflite != null) {
                        val prediction = runInference(faceBitmap)
                        val adjustedBox =
                            adjustBoundingBox(face.boundingBox, bitmap.width, bitmap.height)
                        faceBoxes.add(FaceBox(adjustedBox, prediction))
                    }
                }

                runOnUiThread { overlayView.updateFaces(faceBoxes) }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun adjustBoundingBox(
        originalBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
    ): Rect {
        val scaleX = overlayView.width.toFloat() / imageWidth
        val scaleY = overlayView.height.toFloat() / imageHeight

        val centerX = originalBox.centerX()
        val centerY = originalBox.centerY()
        val width = (originalBox.width() * 1.05).toInt()
        val height = (originalBox.height() * 1.1).toInt()

        val left = (centerX - width / 2).coerceAtLeast(0)
        val top = (centerY - height / 2).coerceAtLeast(0)
        val right = (centerX + width / 2).coerceAtMost(imageWidth)
        val bottom = (centerY + height / 2).coerceAtMost(imageHeight)

        return Rect(
            (imageWidth - right * scaleX).toInt(),
            (top * scaleY).toInt(),
            (imageWidth - left * scaleX).toInt(),
            (bottom * scaleY).toInt(),
        )
    }

    private fun runInference(bitmap: Bitmap): String {
        val input = preprocessBitmap(bitmap)
        val output = Array(1) { FloatArray(7) }
        tflite?.run(input, output)

        val maxConfidence = output[0].maxOrNull() ?: 0f
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val labels =
            arrayOf(
                getString(R.string.text_expression_angry),
                getString(R.string.text_expression_angry),
                getString(R.string.text_expression_angry),
                getString(R.string.text_expression_happy),
                getString(R.string.text_expression_neutral),
                getString(R.string.text_expression_sad),
                getString(R.string.text_expression_surprise),
            )

        return if (maxConfidence > 0.5 && predictedIndex in labels.indices) {
            "${labels[predictedIndex]} (${String.format("%.2f", maxConfidence * 100)}%)"
        } else {
            getString(R.string.text_expression_neutral)
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        // 1. Pastikan format bitmap ARGB_8888 untuk kualitas terbaik
        val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 2. Mirror gambar untuk kamera depan
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        val mirroredBitmap = Bitmap.createBitmap(argbBitmap, 0, 0, argbBitmap.width, argbBitmap.height, matrix, true)

        // 3. Resize gambar ke 48x48 untuk model CNN
        val resizedBitmap = Bitmap.createScaledBitmap(mirroredBitmap, 48, 48, true)
        val buffer = ByteBuffer.allocateDirect(4 * 48 * 48)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = resizedBitmap.getPixel(x, y)
                val grayscale = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114) / 255.0
                buffer.putFloat(grayscale.toFloat())
            }
        }

        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = assets.openFd("cnn_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength,
        )
    }

    private fun cropFace(
        bitmap: Bitmap,
        face: Face,
    ): Bitmap? {
        val box = face.boundingBox

        // Tambahkan padding agar wajah tidak terpotong terlalu ketat
        val paddingFactor = 0.2f // Tambahkan 20% padding
        val width = box.width()
        val height = box.height()
        val extraWidth = (width * paddingFactor).toInt()
        val extraHeight = (height * paddingFactor).toInt()

        val left = (box.left - extraWidth / 2).coerceAtLeast(0)
        val top = (box.top - extraHeight / 2).coerceAtLeast(0)
        val right = (box.right + extraWidth / 2).coerceAtMost(bitmap.width)
        val bottom = (box.bottom + extraHeight / 2).coerceAtMost(bitmap.height)

        val finalWidth = right - left
        val finalHeight = bottom - top

        return if (finalWidth > 0 && finalHeight > 0) {
            Bitmap.createBitmap(bitmap, left, top, finalWidth, finalHeight)
        } else {
            null
        }
    }

    private fun Bitmap.flipHorizontal(): Bitmap {
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        tflite?.close()
        tflite = null
        cameraProvider?.unbindAll()
    }
}
