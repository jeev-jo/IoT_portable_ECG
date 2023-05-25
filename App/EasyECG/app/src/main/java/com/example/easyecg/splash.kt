package com.example.easyecg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.bumptech.glide.Glide
import android.os.Handler
import android.os.Looper
import android.view.View


class splash : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 3000L // Splash screen display time (in milliseconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Load the GIF image using a library like Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)
            .into(findViewById(R.id.splash_image))

        // Delayed execution to transition to the main activity after the splash screen duration
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}
