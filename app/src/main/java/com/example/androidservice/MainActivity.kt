package com.example.androidservice

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidservice.databinding.ActivityMainBinding
import com.example.androidservice.service.MyService


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.start.setOnClickListener {
            if (!isMyServiceRunning(MyService::class.java)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(
                        Intent(
                            this,
                            MyService::class.java
                        )
                    )
                } else {
                    startService(
                        Intent(
                            this,
                            MyService::class.java
                        )
                    )
                }
            } else {
                Toast.makeText(this, "Service is already running", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stop.setOnClickListener {
            stopService(
                Intent(
                    this,
                    MyService::class.java
                )
            )
        }

        MyService.currentTimer.observe(this) {
            binding.counter.text = it.toString()
        }

    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }
}