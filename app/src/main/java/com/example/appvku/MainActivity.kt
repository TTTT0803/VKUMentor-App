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
import com.example.appvku.ui.screen.LoginScreen
import com.example.appvku.ui.screen.SignUpScreen
import com.example.appvku.ui.screen.SplashScreen
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