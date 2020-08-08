package com.src.uscan

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds

class UscanApplication :MultiDexApplication(){


    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, "ca-app-pub-3900921936726126~2209501490");
       /* MobileAds.initialize(
            this
        ) { Log.e("Admob", "Initialized well") }*/
        val context: Context = UscanApplication.applicationContext()

    }

    init {
        instance = this
    }

    companion object {
        private var instance: UscanApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

}