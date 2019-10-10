package com.example.lazyjogger

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import com.example.lazyjogger.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private var heartbeatAnim: AnimatedVectorDrawableCompat? = null
    private var heartRate = 550
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        heartbeatAnim = AnimatedVectorDrawableCompat.create(this, R.drawable.heartbeat)
        fab.setImageDrawable(heartbeatAnim)

        val runnable = HeartbeatRunnable()
        val t = Thread(runnable)
        t.start()

        fab.setOnClickListener {
            val intent = Intent(this, SensorActivity::class.java)
            startActivity(intent)
        }

        if (!checkPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 5)
        }

    }

    inner class HeartbeatRunnable : Runnable {
        override fun run() {
            for (i in 1..1500) {
                heartbeatAnim?.start()
                sleep(heartRate.toLong())
            }
        }
    }

    private fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}

