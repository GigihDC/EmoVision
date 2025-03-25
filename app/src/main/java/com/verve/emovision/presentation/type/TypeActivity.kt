package com.verve.emovision.presentation.type

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.verve.emovision.data.model.Expression
import com.verve.emovision.data.source.ItemExpression
import com.verve.emovision.databinding.ActivityTypeBinding
import com.verve.emovision.presentation.detail.DetailActivity
import com.verve.emovision.presentation.type.adapter.ExpressionAdapter

class TypeActivity : AppCompatActivity() {
    private val binding: ActivityTypeBinding by lazy {
        ActivityTypeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnClickListener()
        loadExpressionData()
    }

    private fun setOnClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadExpressionData() {
        binding.pbLoadingExpression.isVisible = true
        binding.rvExpression.isVisible = false

        val expressions = ItemExpression.itemList
        setupRecyclerView(expressions)
    }

    private fun setupRecyclerView(expressions: List<Expression>) {
        binding.pbLoadingExpression.isVisible = false
        binding.rvExpression.isVisible = true

        val adapterExpression =
            ExpressionAdapter(expressions) { selectedItem ->
                onItemClick(selectedItem)
            }

        binding.rvExpression.apply {
            adapter = adapterExpression
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun onItemClick(item: Expression) {
        val intent =
            Intent(this, DetailActivity::class.java).apply {
                putExtra("EXTRA_EXPRESSION", item)
            }
        startActivity(intent)
    }
}
