package com.example.appvku.ui.component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavHostController,
    drawerState: DrawerState,
    userRole: String?,
    isRoleLoading: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE5F0FF))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "VKU Mentor",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    if (isRoleLoading) {
                        Log.d("AppTopBar", "Role chưa tải xong, đang đợi...")
                    } else {
                        Log.d("AppTopBar", "Role hiện tại: $userRole")
                        if (userRole?.lowercase() == "mentor") {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Bạn đã là mentor!",
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else if (userRole?.lowercase() == "mentee") {
                            navController.navigate("register_mentor")
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Vui lòng đăng nhập để đăng ký mentor!",
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                enabled = !isRoleLoading
            ) {
                Image(
                    painter = painterResource(id = R.drawable.register),
                    contentDescription = "Đăng ký Mentor",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { navController.navigate("community") }) {
                Image(
                    painter = painterResource(id = R.drawable.group),
                    contentDescription = "Cộng đồng",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { navController.navigate("collaboration") }) {
                Image(
                    painter = painterResource(id = R.drawable.hoptac),
                    contentDescription = "Hợp tác",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { navController.navigate("rating") }) {
                Image(
                    painter = painterResource(id = R.drawable.rating),
                    contentDescription = "Đánh giá",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { navController.navigate("about_us") }) {
                Image(
                    painter = painterResource(id = R.drawable.vechungto),
                    contentDescription = "Về chúng tớ",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AppBottomBar(
    navController: NavHostController,
    selectedItem: String
) {
    val authState = LocalAuthState.current

    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.trangchu),
                        contentDescription = "Trang chủ",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Trang chủ", fontSize = 12.sp) },
                selected = selectedItem == "home",
                onClick = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Profile", fontSize = 12.sp) },
                selected = selectedItem == "profile",
                onClick = {
                    navController.navigate("update_profile")
                }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Đăng xuất",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Đăng xuất", fontSize = 12.sp) },
                selected = false,
                onClick = {
                    authState.signOut()
                    navController.navigate("splash") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}