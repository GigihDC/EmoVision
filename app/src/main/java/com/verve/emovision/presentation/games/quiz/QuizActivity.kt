package com.verve.emovision.presentation.games.quiz

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.verve.emovision.R
import com.verve.emovision.data.model.Question
import com.verve.emovision.data.source.ItemQuestions
import com.verve.emovision.databinding.ActivityQuizBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {
    private val binding: ActivityQuizBinding by lazy {
        ActivityQuizBinding.inflate(layoutInflater)
    }
    private lateinit var questions: List<Question>
    private var currentQuestionIndex = 0
    private var score = 0
    private var questionsAnswered = 0
    private val pointsPerQuestion = 10
    private var hasStarted = false
    private val options =
        listOf(
            binding.btnOptions1,
            binding.btnOptions2,
            binding.btnOptions3,
            binding.btnOptions4,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        questions = ItemQuestions.getQuestions().shuffled().take(10)
        setOnClickListener()
        showTapToStart()
    }

    private fun setOnClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.root.setOnClickListener {
            if (!hasStarted) {
                hasStarted = true
                hideTapToStart()
                displayQuestion()
            }
        }
    }

    private fun showTapToStart() {
        binding.tvTapToStart.visibility = View.VISIBLE
        options.forEach { button ->
            button.isVisible = false
        }
    }

    private fun hideTapToStart() {
        binding.tvTapToStart.visibility = View.GONE
        options.forEach { button ->
            button.isVisible = true
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            binding.tvQuestion.setText(question.question)

            options.forEachIndexed { index, button ->
                button.setText(question.options[index])
                button.setOnClickListener {
                    if (hasStarted) {
                        handleAnswer(index)
                    }
                }
            }
        }
    }

    private fun handleAnswer(selectedIndex: Int) {
        val correctAnswerIndex = questions[currentQuestionIndex].correctAnswer
        if (selectedIndex == correctAnswerIndex) {
            score += pointsPerQuestion
        }
        questionsAnswered++
        currentQuestionIndex++

        lifecycleScope.launch {
            delay(500)
            if (questionsAnswered == 10) {
                showScore()
            } else {
                displayQuestion()
            }
        }
    }

    private fun showScore() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quiz_result, null)
        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

        val progressCorrect = dialogView.findViewById<ProgressBar>(R.id.progress_correct)
        val progressWrong = dialogView.findViewById<ProgressBar>(R.id.progress_wrong)
        val tvScorePercentage = dialogView.findViewById<TextView>(R.id.tv_score_percentage)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        val totalQuestions = 10
        val maxScore = totalQuestions * pointsPerQuestion
        val percentage = ((score.toFloat() / maxScore) * 100).toInt()

        progressCorrect.progress = 100
        progressWrong.progress = 100
        tvScorePercentage.text = "$percentage%"

        val layoutParams = tvScorePercentage.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.horizontalBias = (percentage / 100f).coerceIn(0.1f, 0.9f)
        tvScorePercentage.layoutParams = layoutParams

        val dismissDialog = {
            dialog.dismiss()
            finish()
        }
        btnOk.setOnClickListener {
            dismissDialog()
        }
        dialog.setOnCancelListener {
            dismissDialog()
        }

        dialog.show()
    }
}
