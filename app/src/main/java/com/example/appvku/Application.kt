package com.example.appvku

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.appvku.DatabaseInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Cloudinary
        val config = mapOf(
            "cloud_name" to "dqs4tuaru",
            "api_key" to "252599453453589",
            "api_secret" to "6umMu92EzRsMmzbNeuv60OHXEno"
        )
        MediaManager.init(this, config)

//        // Khởi tạo cơ sở dữ liệu
//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                DatabaseInitializer.initializeDatabase()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }
}