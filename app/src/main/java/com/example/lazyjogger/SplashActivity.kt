package com.example.lazyjogger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val kuva = findViewById<ImageView>(R.id.kuva)
        Glide.with(this).load(R.raw.heart).into(kuva)

        Thread(Runnable {
            sleep(2500)
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }).start()
    }
}
