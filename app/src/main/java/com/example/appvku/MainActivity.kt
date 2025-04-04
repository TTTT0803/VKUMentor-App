package com.example.appvku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appvku.ui.screen.SplashScreen
import com.example.appvku.ui.screen.LoginScreen
import com.example.appvku.ui.screen.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash_screen") {
                composable("splash_screen") { SplashScreen(navController) }
                composable("login") { LoginScreen(navController) } // ✅ Thêm LoginScreen vào NavHost
                composable("signup") { SignUpScreen(navController) } // ✅ Thêm LoginScreen vào NavHost

            }
        }
    }
}
