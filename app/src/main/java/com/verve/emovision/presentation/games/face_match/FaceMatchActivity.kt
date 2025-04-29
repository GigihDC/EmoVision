package com.verve.emovision.presentation.games.face_match

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
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
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.verve.emovision.R
import com.verve.emovision.databinding.ActivityFaceMatchBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceMatchActivity : AppCompatActivity() {
    private val binding: ActivityFaceMatchBinding by lazy {
        ActivityFaceMatchBinding.inflate(layoutInflater)
    }
    private var score = 0
    private lateinit var gameView: GameView
    private var targetExpression: String = "Neutral"
    private lateinit var cameraExecutor: ExecutorService
    private var tflite: Interpreter? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var hasStarted = false
    private var bgmPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        gameView = binding.gameView
        setOnClickListener()
        showTapToStart()
        updateScoreUI()
        initTensorFlow()
        initCameraExecutor()
        setNextExpression()
    }

    private fun setOnClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.root.setOnClickListener {
            if (!hasStarted) {
                hasStarted = true
                hideTapToStart()
                requestPermissions()
                startBgm()
            }
        }
    }

    private fun showTapToStart() {
        binding.tvTapToStart.visibility = View.VISIBLE
    }

    private fun hideTapToStart() {
        binding.tvTapToStart.visibility = View.GONE
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
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startBgm() {
        val afd = assets.openFd("bgm_game.mp3")
        bgmPlayer =
            MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                prepare()
                start()
            }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val detector =
            FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .build(),
            )
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val bitmap = imageProxy.toBitmap()
                    val prediction = runInference(bitmap)
                    checkExpression(prediction)
                }
                updateExpressionOverlay(targetExpression)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun runInference(bitmap: Bitmap): String {
        val input = preprocessBitmap(bitmap)
        val output = Array(1) { FloatArray(7) }
        tflite?.run(input, output)
        val labels = arrayOf("Angry", "Angry", "Angry", "Happy", "Neutral", "Sad", "Surprise")
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return if (predictedIndex in labels.indices) labels[predictedIndex] else "Neutral"
    }

    private fun checkExpression(predicted: String) {
        if (score >= 10) return
        if (predicted == targetExpression) {
            score++
            updateScoreUI()
            if (score >= 10) {
                showCompletionDialog()
            } else {
                setNextExpression()
            }
        }
    }

    private fun updateScoreUI() {
        runOnUiThread {
            binding.tvScore.text = score.toString()
        }
    }

    private fun setNextExpression() {
        targetExpression = getNextExpression()
        updateExpressionOverlay(targetExpression)
        gameView.updatePlayerExpression(targetExpression)
    }

    private fun updateExpressionOverlay(expression: String) {
        runOnUiThread {
            drawEmojiOverlay(expression)
        }
    }

    private fun showCompletionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_completed, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(true)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.btn_restart).setOnClickListener {
            dialog.dismiss()
            restartGame()
        }
        dialogView.findViewById<Button>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.setOnCancelListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun restartGame() {
        score = 0
        updateScoreUI()
        setNextExpression()
    }

    private fun drawEmojiOverlay(expression: String) {
        val emojiMap =
            mapOf(
                "Happy" to R.drawable.img_emoji_happy,
                "Sad" to R.drawable.img_emoji_sad,
                "Surprise" to R.drawable.img_emoji_surprise,
                "Angry" to R.drawable.img_emoji_angry,
                "Neutral" to R.drawable.img_emoji_neutral,
            )

        val emojiResId = emojiMap[expression] ?: return
        runOnUiThread {
            binding.ivEmoji.setImageResource(emojiResId)
            binding.ivEmoji.visibility = View.VISIBLE
        }
    }

    private fun getNextExpression(): String {
        val expressionList = listOf("Happy", "Sad", "Surprise", "Angry", "Neutral")
        val nextIndex = (expressionList.indexOf(targetExpression) + 1) % expressionList.size
        return expressionList[nextIndex]
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        val mirroredBitmap =
            Bitmap.createBitmap(argbBitmap, 0, 0, argbBitmap.width, argbBitmap.height, matrix, true)

        val resizedBitmap = Bitmap.createScaledBitmap(mirroredBitmap, 48, 48, true)

        val grayscaleBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(resizedBitmap, 0f, 0f, paint)

        val byteBuffer =
            ByteBuffer.allocateDirect(1 * 48 * 48 * 1 * 4)
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        tflite?.close()
        tflite = null
        cameraProvider?.unbindAll()
        bgmPlayer?.release()
        bgmPlayer = null
    }
}
