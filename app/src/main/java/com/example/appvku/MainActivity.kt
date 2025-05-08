package com.example.appvku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appvku.ui.screen.CommunityScreen
import com.example.appvku.ui.screen.EditMentorScreen
import com.example.appvku.ui.screen.HomeScreen
import com.example.appvku.ui.screen.LoginScreen
import com.example.appvku.ui.screen.PendingApprovalScreen
import com.example.appvku.ui.screen.RegisterMentorScreen
import com.example.appvku.ui.screen.SplashScreen
import com.example.appvku.ui.theme.AppVKUTheme
import com.google.firebase.auth.FirebaseAuth
import com.example.appvku.ui.screen.SignUpScreen
import com.example.appvku.AuthManager
import com.cloudinary.android.MediaManager
import com.example.appvku.ui.screen.AboutUsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo MediaManager (Cloudinary) một lần duy nhất
        val config = mapOf(
            "cloud_name" to "dqs4tuaru",
            "api_key" to "252599453453589",
            "api_secret" to "6umMu92EzRsMmzbNeuv60OHXEno"
        )
        try {
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            // MediaManager đã được khởi tạo, bỏ qua lỗi này
        } catch (e: Exception) {
            e.printStackTrace() // Xử lý các lỗi khác nếu có
        }

        // Khởi tạo dữ liệu mẫu
//        SampleDataInitializer.initializeSampleData()

        setContent {
            AppVKUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf("splash") }
                    var role by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            AuthManager.fetchUserRole(user.uid) { fetchedRole ->
                                role = fetchedRole
                                startDestination = when (fetchedRole?.lowercase()) {
                                    "admin" -> "pending_approval"
                                    else -> "home"
                                }
                                navController.navigate(startDestination) {
                                    popUpTo("splash") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            startDestination = "splash"
                            navController.navigate(startDestination) {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("splash") { SplashScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("community") { CommunityScreen(navController) }
                        composable("register_mentor") { RegisterMentorScreen(navController) }
                        composable("pending_approval") { PendingApprovalScreen(navController) }
                        composable("signup_screen") { SignUpScreen(navController) }

                        composable("about_us") { AboutUsScreen(navController) }

                        composable(
                            "edit_mentor/{mentorId}/{name}/{expertise}/{organization}/{achievements}/{referralSource}/{image}",
                            arguments = listOf(
                                navArgument("mentorId") { type = NavType.StringType },
                                navArgument("name") { type = NavType.StringType },
                                navArgument("expertise") { type = NavType.StringType },
                                navArgument("organization") { type = NavType.StringType },
                                navArgument("achievements") { type = NavType.StringType },
                                navArgument("referralSource") { type = NavType.StringType },
                                navArgument("image") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val mentorId = backStackEntry.arguments?.getString("mentorId")
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            val expertise = backStackEntry.arguments?.getString("expertise") ?: ""
                            val organization = backStackEntry.arguments?.getString("organization") ?: ""
                            val achievements = backStackEntry.arguments?.getString("achievements") ?: ""
                            val referralSource = backStackEntry.arguments?.getString("referralSource") ?: ""
                            val image = backStackEntry.arguments?.getString("image") ?: ""
                            EditMentorScreen(
                                navController = navController,
                                mentorId = mentorId,
                                name = name,
                                expertise = expertise,
                                organization = organization,
                                achievements = achievements,
                                referralSource = referralSource,
                                image = image
                            )
                        }
                    }
                }
            }
        }
    }
}