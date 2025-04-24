package com.verve.emovision.presentation.help

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verve.emovision.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {
    private val binding: ActivityHelpBinding by lazy {
        ActivityHelpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }
}
