package com.verve.emovision.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verve.emovision.databinding.ActivityMainBinding
import com.verve.emovision.presentation.about.AboutActivity
import com.verve.emovision.presentation.games.GameActivity
import com.verve.emovision.presentation.scan.ScanActivity
import com.verve.emovision.presentation.type.TypeActivity

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ivScan.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        binding.ivGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        binding.ivType.setOnClickListener {
            startActivity(Intent(this, TypeActivity::class.java))
        }
        binding.ivAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
