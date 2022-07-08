package com.test.smartpot

import android.app.Application

class myApp : Application(){
    companion object {
        lateinit var prefs: Preutil
    }

    override fun onCreate() {
        prefs = Preutil(applicationContext)
        super.onCreate()
    }
}