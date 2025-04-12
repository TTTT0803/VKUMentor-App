package com.example.appvku

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appvku.ui.screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash_screen") {
                composable("splash_screen") { SplashScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignUpScreen(navController) }
                composable("home") { HomeScreen(navController) } // Thêm route cho HomeScreen
                // Các route cho drawer
                composable("register_mentor") { /* TODO: Tạo màn hình Đăng ký Mentor */ }
                composable("search_mentor") { /* TODO: Tạo màn hình Tìm kiếm Mentor */ }
                composable("community") { /* TODO: Tạo màn hình Cộng đồng */ }
                composable("collaboration") { /* TODO: Tạo màn hình Hợp tác */ }
                composable("rating") { /* TODO: Tạo màn hình Đánh giá */ }
                composable("about_us") { /* TODO: Tạo màn hình Về chúng tớ */ }
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Log.d(TAG, "Starting database initialization...")
                    DatabaseInitializer.initializeDatabase()
                    Toast.makeText(this@MainActivity, "Database initialized successfully!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Database initialized successfully!")
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error initializing database: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error initializing database: ${e.message}", e)
                }
            }
        }
    }
}