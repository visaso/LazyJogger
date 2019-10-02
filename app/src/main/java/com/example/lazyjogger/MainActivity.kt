package com.example.lazyjogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import com.example.lazyjogger.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private var heartbeatAnim: AnimatedVectorDrawableCompat? = null
    private var heartrate = 550

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

        fab.setOnClickListener { view ->
        }

    }
    inner class HeartbeatRunnable: Runnable {
        override fun run() {
            for (i in 1..500) {
                heartbeatAnim?.start()
                sleep(heartrate.toLong())
            }
        }
    }
}

