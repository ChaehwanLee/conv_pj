package com.test.smartpot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class splashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    lateinit var threadObj:Thread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler = Handler(Looper.myLooper()!!)

        threadObj = object  :Thread(){
            override fun run() {
                super.run()
                val intent = Intent(applicationContext,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        handler.postDelayed(threadObj,1000)
    }
}