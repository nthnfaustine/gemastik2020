package com.example.gemastik

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class Splash: Activity() {
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@Splash, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}