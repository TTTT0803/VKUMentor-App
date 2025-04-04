package com.example.appvku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(navController: NavHostController) { // ✅ Đúng tham số

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE5F0FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Connect, Learn, and\nGrow with VKUMentor",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E),
                modifier = Modifier.offset(y = (-120).dp) // Kéo lên trên 8.dp
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "VKUMentor Logo",
                modifier = Modifier
                    .size(150.dp) // Kích thước ảnh gốc
                    .scale(2.0f)  // Phóng to 1.8 lần
            )


            Button(
                onClick = {
                    navController.navigate("login")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .padding(top = 50.dp) // Thêm khoảng cách từ phía trên
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Text(
                text = "Already have an account? Sign in",
                modifier = Modifier
                    .padding(top = 50.dp)
                    .clickable {
                        // Điều hướng đến trang đăng nhập
                        navController.navigate("login")
                    },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )

        }
    }
}
