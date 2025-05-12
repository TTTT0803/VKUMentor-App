package com.example.appvku

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appvku.ui.screen.AboutUsScreen
import com.example.appvku.ui.screen.CommunityScreen
import com.example.appvku.ui.screen.CooperationScreen
import com.example.appvku.ui.screen.EditMentorScreen
import com.example.appvku.ui.screen.HomeScreen
import com.example.appvku.ui.screen.LoginScreen
import com.example.appvku.ui.screen.MentorDetailScreen
import com.example.appvku.ui.screen.PendingApprovalScreen
import com.example.appvku.ui.screen.RatingScreen
import com.example.appvku.ui.screen.RegisterMentorScreen
import com.example.appvku.ui.screen.SignUpScreen
import com.example.appvku.ui.screen.SplashScreen
import com.example.appvku.ui.theme.AppVKUTheme
import com.cloudinary.android.MediaManager
import com.example.appvku.ui.screen.UpdateProfileScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Cloudinary
        val config = mapOf(
            "cloud_name" to "dqs4tuaru",
            "api_key" to "252599453453589",
            "api_secret" to "6umMu92EzRsMmzbNeuv60OHXEno"
        )
        try {
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            Log.e("MainActivity", "Cloudinary đã được khởi tạo trước đó: ${e.message}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khởi tạo Cloudinary: ${e.message}", e)
            e.printStackTrace()
        }

//        SampleDataInitializer.initializeSampleData()

        setContent {
            AppVKUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState = remember { AuthState() }
                    CompositionLocalProvider(LocalAuthState provides authState) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authState = LocalAuthState.current

    LaunchedEffect(authState.currentUser, authState.isRoleLoading, authState.userRole) {
        if (authState.isRoleLoading) {
            Log.d("AppNavigation", "Đang tải vai trò, chưa điều hướng...")
            return@LaunchedEffect
        }

        Log.d("AppNavigation", "Vai trò đã tải xong: userRole = ${authState.userRole}")

        val startDestination = if (authState.currentUser == null) {
            Log.d("AppNavigation", "Không có người dùng, điều hướng đến splash")
            "splash"
        } else {
            when (authState.userRole?.lowercase()) {
                "admin" -> {
                    Log.d("AppNavigation", "Người dùng là admin, điều hướng đến pending_approval")
                    "pending_approval"
                }
                "mentor" -> {
                    Log.d("AppNavigation", "Người dùng là mentor, điều hướng đến home")
                    "home"
                }
                "mentee" -> {
                    Log.d("AppNavigation", "Người dùng là mentee, điều hướng đến home")
                    "home"
                }
                else -> {
                    Log.d("AppNavigation", "Vai trò không xác định (${authState.userRole}), điều hướng đến home")
                    "home"
                }
            }
        }

        navController.navigate(startDestination) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("community") { CommunityScreen(navController) }
        composable("register_mentor") { RegisterMentorScreen(navController) }
        composable("pending_approval") { PendingApprovalScreen(navController) }
        composable("signup_screen") { SignUpScreen(navController) }
        composable("collaboration") { CooperationScreen(navController) }
        composable("about_us") { AboutUsScreen(navController) }
        composable("update_profile") { UpdateProfileScreen(navController) }
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
            val name = backStackEntry.arguments?.getString("name")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val expertise = backStackEntry.arguments?.getString("expertise")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val organization = backStackEntry.arguments?.getString("organization")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val achievements = backStackEntry.arguments?.getString("achievements")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val referralSource = backStackEntry.arguments?.getString("referralSource")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val image = backStackEntry.arguments?.getString("image")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""

            if (mentorId != null) {
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
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(
            "mentor_detail/{mentorId}",
            arguments = listOf(navArgument("mentorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mentorId = backStackEntry.arguments?.getString("mentorId")
            if (mentorId != null) {
                MentorDetailScreen(navController = navController, mentorId = mentorId)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable("rating") { RatingScreen(navController) }
    }
}