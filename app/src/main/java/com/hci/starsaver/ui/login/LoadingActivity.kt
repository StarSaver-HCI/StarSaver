package com.hci.starsaver.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.hci.starsaver.MainActivity
import com.hci.starsaver.databinding.ActivityLoadingBinding


class LoadingActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLoading()
    }

    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed(Runnable { startActivity(Intent(this@LoadingActivity,MainActivity::class.java)) }, 2000)
    }
}