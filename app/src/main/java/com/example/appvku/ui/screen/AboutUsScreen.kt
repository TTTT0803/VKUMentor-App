package com.example.appvku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import com.example.appvku.ui.component.AppBottomBar
import com.example.appvku.ui.component.AppTopBar
import com.example.appvku.ui.component.DrawerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val authState = LocalAuthState.current
    val selectedItem by remember { mutableStateOf("about_us") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController) { scope.launch { drawerState.close() } }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppTopBar(
                    navController = navController,
                    drawerState = drawerState,
                    userRole = authState.userRole,
                    isRoleLoading = authState.isRoleLoading,
                    snackbarHostState = snackbarHostState
                )
            },
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    selectedItem = selectedItem
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.about_us_banner2),
                            contentDescription = "About Us Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f)),
                                        startY = 100f
                                    )
                                )
                        )
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 30.dp)
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "About Us",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = "App VKU là một ứng dụng học tập và cộng đồng được phát triển bởi sinh viên trường Đại học VKU. Ứng dụng cung cấp các tính năng như chia sẻ tài liệu, kết nối sinh viên, hỗ trợ học tập và quản lý hoạt động nhóm.",
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Thành viên nhóm phát triển",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MemberCard(
                        name = "Thanh Thảo",
                        clazz = "23SE2",
                        role = "Software Dev",
                        imageRes = R.drawable.thanhthao
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MemberCard(
                        name = "Nguyễn Quang Kính",
                        clazz = "23SE2",
                        role = "Software Dev",
                        imageRes = R.drawable.quangkinh
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        )
    }
}

@Composable
fun MemberCard(
    name: String,
    clazz: String,
    role: String,
    imageRes: Int
) {
    var showIcons by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onClick = { showIcons = !showIcons }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = clazz, fontSize = 14.sp, color = Color.Gray)
                    Text(text = role, fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (showIcons) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { /* TODO: Facebook */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_facebook),
                            contentDescription = "Facebook",
                            tint = Color(0xFF3b5998)
                        )
                    }
                    IconButton(onClick = { /* TODO: Email */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = "Email",
                            tint = Color(0xFFDB4437)
                        )
                    }
                    IconButton(onClick = { /* TODO: Phone */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Phone",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
        }
    }
}