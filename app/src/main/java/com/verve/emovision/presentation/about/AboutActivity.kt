package com.verve.emovision.presentation.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verve.emovision.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private val binding: ActivityAboutBinding by lazy {
        ActivityAboutBinding.inflate(layoutInflater)
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
