package com.verve.emovision.presentation.games

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.verve.emovision.data.model.Game
import com.verve.emovision.data.source.ItemGame
import com.verve.emovision.databinding.ActivityGameBinding
import com.verve.emovision.presentation.detail.DetailActivity
import com.verve.emovision.presentation.games.adapter.GameAdapter
import com.verve.emovision.presentation.games.face_match.FaceMatchActivity
import com.verve.emovision.presentation.games.quiz.QuizActivity

class GameActivity : AppCompatActivity() {
    private val binding: ActivityGameBinding by lazy {
        ActivityGameBinding.inflate(layoutInflater)
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
        binding.pbLoadingGames.isVisible = true
        binding.rvGames.isVisible = false

        val games = ItemGame.itemList
        setupRecyclerView(games)
    }

    private fun setupRecyclerView(games: List<Game>) {
        binding.pbLoadingGames.isVisible = false
        binding.rvGames.isVisible = true

        val adapterGame =
            GameAdapter(games) { selectedItem ->
                onItemClick(selectedItem)
            }

        binding.rvGames.apply {
            adapter = adapterGame
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun onItemClick(item: Game) {
        val intent =
            when (item.id) {
                1 -> Intent(this, FaceMatchActivity::class.java)
                2 -> Intent(this, QuizActivity::class.java)
                else -> Intent(this, DetailActivity::class.java)
            }.apply {
                putExtra("EXTRA_GAME", item)
            }
        startActivity(intent)
    }
}
