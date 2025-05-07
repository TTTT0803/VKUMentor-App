package com.example.appvku.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun DrawerContent(navController: NavHostController, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Trang chủ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("home")
                onClose()
            }
        )
        Text(
            text = "Đăng ký Mentor",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("register_mentor")
                onClose()
            }
        )
        Text(
            text = "Tìm kiếm Mentor",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("search_mentor")
                onClose()
            }
        )
        Text(
            text = "Cộng đồng",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("community")
                onClose()
            }
        )
        Text(
            text = "Hợp tác",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("collaboration")
                onClose()
            }
        )
        Text(
            text = "Đánh giá",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("rating")
                onClose()
            }
        )
        Text(
            text = "Về chúng tớ",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("about_us")
                onClose()
            }
        )
    }
}