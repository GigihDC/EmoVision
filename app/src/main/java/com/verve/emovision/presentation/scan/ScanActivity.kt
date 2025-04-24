package com.verve.emovision.presentation.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
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
import com.verve.emovision.presentation.help.HelpActivity
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
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var tflite: Interpreter? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        overlayView = findViewById(R.id.overlayView)
        setOnClickListener()
        initTensorFlow()
        requestPermissions()
        initCameraExecutor()
    }

    private fun setOnClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        binding.btnSwitch.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            animatePreviewFlip()
            startCamera()
        }
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
                    it.setAnalyzer(cameraExecutor) { imageProxy -> analyzeImage(imageProxy) }
                }

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun animatePreviewFlip() {
        binding.previewView.animate()
            .scaleX(0f)
            .setDuration(250)
            .withEndAction {
                binding.previewView.rotationY += 180f
                binding.previewView.animate()
                    .scaleX(1f)
                    .setDuration(250)
                    .start()
            }
            .start()
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

                val bitmap = imageProxy.toBitmap()
                val isFrontCamera = (lensFacing == CameraSelector.LENS_FACING_FRONT)

                for (face in faces) {
                    val faceBitmap = cropFace(bitmap, face)

                    if (faceBitmap != null && tflite != null) {
                        val prediction = runInference(faceBitmap, isFrontCamera)
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

    private fun runInference(
        bitmap: Bitmap,
        isFrontCamera: Boolean,
    ): String {
        val input = preprocessBitmap(bitmap, isFrontCamera)
        val output = Array(1) { FloatArray(7) }
        tflite?.run(input, output)

        val maxConfidence = output[0].maxOrNull() ?: 0f
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val labels =
            arrayOf(
                getString(R.string.text_expression_angry),
                getString(R.string.text_expression_disgust),
                getString(R.string.text_expression_fear),
                getString(R.string.text_expression_happy),
                getString(R.string.text_expression_neutral),
                getString(R.string.text_expression_sad),
                getString(R.string.text_expression_surprise),
            )

        return if (maxConfidence > 0.4 && predictedIndex in labels.indices) {
            "${labels[predictedIndex]} (${String.format("%.2f", maxConfidence * 100)}%)"
        } else {
            getString(R.string.text_expression_neutral)
        }
    }

    private fun preprocessBitmap(
        bitmap: Bitmap,
        isFrontCamera: Boolean,
    ): ByteBuffer {
        val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 2. Mirror gambar kalau kamera depan
        val processedBitmap =
            if (isFrontCamera) {
                val matrix = Matrix().apply { preScale(-1f, 1f) }
                Bitmap.createBitmap(
                    argbBitmap,
                    0,
                    0,
                    argbBitmap.width,
                    argbBitmap.height,
                    matrix,
                    true,
                )
            } else {
                argbBitmap
            }

        // 3. Resize ke 48x48
        val resizedBitmap = Bitmap.createScaledBitmap(processedBitmap, 48, 48, true)

        // 4. Konversi ke grayscale pakai ColorMatrix
        val grayscaleBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(resizedBitmap, 0f, 0f, paint)

        // 5. Buat ByteBuffer untuk input model
        val byteBuffer =
            ByteBuffer.allocateDirect(1 * 48 * 48 * 1 * 4) // 1 (batch) × 48 × 48 × 1 (grayscale) × 4 bytes (float32)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = grayscaleBitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val normalizedPixel = r / 255.0f
                byteBuffer.putFloat(normalizedPixel)
            }
        }

        byteBuffer.rewind()
        return byteBuffer
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
