package com.verve.emovision.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.verve.emovision.R
import com.verve.emovision.databinding.ActivityMainBinding
import com.verve.emovision.presentation.about.AboutActivity
import com.verve.emovision.presentation.games.GameActivity
import com.verve.emovision.presentation.scan.ScanActivity
import com.verve.emovision.presentation.type.TypeActivity

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var backPressedTime: Long = 0
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideStatusBar()
        setOnClickListener()
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()
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

    @Deprecated(
        "This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.",
    )
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            toast.cancel()
            super.onBackPressed()
            return
        } else {
            toast =
                Toast.makeText(
                    this,
                    getString(R.string.text_on_back_pressed), Toast.LENGTH_SHORT,
                )
            toast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
