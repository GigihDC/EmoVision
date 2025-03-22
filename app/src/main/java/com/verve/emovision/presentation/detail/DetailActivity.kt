package com.verve.emovision.presentation.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verve.emovision.data.model.Expression
import com.verve.emovision.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }
    private var expression: Expression? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnClickListener()
        bindExpression()
    }

    private fun setOnClickListener() {
        binding.ibBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun bindExpression() {
        expression = intent.getParcelableExtra("EXTRA_EXPRESSION") ?: return

        expression?.let { item ->
            binding.svDetailExpression.tvExpressionName.setText(item.name)
            binding.svDetailExpression.tvExpressionDescription.setText(item.desc)
            binding.svDetailExpression.ivExpressionImage.setImageResource(item.img)
        }
    }
}
