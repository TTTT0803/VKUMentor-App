package com.example.appvku.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appvku.AuthManager
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import com.example.appvku.ui.component.DrawerContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    var currentRole by remember { mutableStateOf<String?>(null) }
    var isRoleLoading by remember { mutableStateOf(true) } // Trạng thái tải role
    val snackbarHostState = remember { SnackbarHostState() } // State cho Snackbar

    // Lấy role của người dùng hiện tại
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            AuthManager.fetchUserRole(user.uid) { role ->
                Log.d("HomeScreen", "Role fetched: $role")
                currentRole = role
                isRoleLoading = false
            }
        } else {
            isRoleLoading = false
        }
    }

    // State để quản lý danh sách mentor hiển thị
    var visibleMentors by remember { mutableStateOf(listOf<MentorInfo>()) }
    var lastDocument by remember { mutableStateOf<com.google.firebase.firestore.DocumentSnapshot?>(null) }
    var page by remember { mutableStateOf(0) }
    val mentorsPerPage = 9
    val additionalMentors = 6
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State để theo dõi vị trí cuộn
    val gridState = rememberLazyGridState()

    // State để theo dõi mục được chọn trong NavigationBar
    var selectedItem by remember { mutableStateOf("home") }

    // Tải dữ liệu từ Firestore
    fun loadMentors() {
        isLoading = true
        errorMessage = null
        val db = FirebaseFirestore.getInstance()
        db.collection("mentor_info")
            .whereEqualTo("status", "approved")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(mentorsPerPage.toLong())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("HomeScreen", "Không tìm thấy mentor nào được phê duyệt.")
                    errorMessage = "Không có mentor nào để hiển thị."
                } else {
                    val fetchedMentors = documents.map { doc ->
                        val mentor = doc.toObject(MentorInfo::class.java)
                        mentor.copy(id = doc.id)
                    }
                    Log.d("HomeScreen", "Tải được ${fetchedMentors.size} mentor: $fetchedMentors")
                    visibleMentors = fetchedMentors
                    lastDocument = documents.documents.lastOrNull()
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("HomeScreen", "Lỗi khi tải mentor: ${exception.message}", exception)
                errorMessage = "Lỗi khi tải dữ liệu: ${exception.message}"
                isLoading = false
            }
    }

    fun loadMoreMentors() {
        if (lastDocument != null && !isLoading) {
            isLoading = true
            val db = FirebaseFirestore.getInstance()
            db.collection("mentor_info")
                .whereEqualTo("status", "approved")
                .orderBy("name", Query.Direction.ASCENDING)
                .startAfter(lastDocument)
                .limit(additionalMentors.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val newMentors = documents.map { doc ->
                            val mentor = doc.toObject(MentorInfo::class.java)
                            mentor.copy(id = doc.id)
                        }
                        visibleMentors = visibleMentors + newMentors
                        lastDocument = documents.documents.lastOrNull()
                    } else {
                        lastDocument = null
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeScreen", "Lỗi khi tải thêm mentor: ${exception.message}")
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        loadMentors()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController) { scope.launch { drawerState.close() } }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }, // Thêm SnackbarHost
            topBar = {
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
                                    Log.d("HomeScreen", "Role chưa tải xong, đang đợi...")
                                } else {
                                    Log.d("HomeScreen", "Role hiện tại: $currentRole")
                                    if (currentRole?.lowercase() == "mentor") {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bạn đã là mentor!",
                                                actionLabel = "OK",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        navController.navigate("register_mentor")
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
                        IconButton(onClick = { navController.navigate("search_mentor") }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm Mentor")
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
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5F0FF))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                ) {
                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.trangchu),
                                contentDescription = "Trang chủ",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Trang chủ",
                                fontSize = 12.sp,
                                color = if (selectedItem == "home") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "home",
                        onClick = {
                            selectedItem = "home"
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.thongbao),
                                contentDescription = "Thông báo",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Thông báo",
                                fontSize = 12.sp,
                                color = if (selectedItem == "notifications") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "notifications",
                        onClick = {
                            selectedItem = "notifications"
                            navController.navigate("notifications") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.taikhoan),
                                contentDescription = "Tài khoản",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Tài khoản",
                                fontSize = 12.sp,
                                color = if (selectedItem == "profile") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "profile",
                        onClick = {
                            selectedItem = "profile"
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
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
                        label = {
                            Text(
                                text = "Đăng xuất",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        selected = false,
                        onClick = {
                            AuthManager.signOut()
                            navController.navigate("splash") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5F0FF))
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Mentor nổi bật",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (errorMessage != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Đã có lỗi xảy ra",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                state = gridState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(visibleMentors) { mentor ->
                                    MentorCard(mentor)
                                }

                                item(span = { GridItemSpan(3) }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (visibleMentors.size > mentorsPerPage) {
                                            Button(
                                                onClick = {
                                                    isLoading = true
                                                    loadMentors()
                                                },
                                                modifier = Modifier.width(120.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "Ẩn",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.width(120.dp))
                                        }

                                        if (lastDocument != null) {
                                            Button(
                                                onClick = { loadMoreMentors() },
                                                modifier = Modifier.width(120.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "Xem thêm",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.width(120.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MentorCard(mentor: MentorInfo) {
    val context = LocalContext.current

    // Thay thế http:// thành https:// trong URL
    val secureImageUrl = mentor.image?.replace("http://", "https://") ?: R.drawable.nguyenquangkinh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Xử lý khi nhấn vào Mentor */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(secureImageUrl)
                    .crossfade(true)
                    .listener(
                        onStart = {
                            Log.d("MentorCard", "Bắt đầu tải hình ảnh: $secureImageUrl")
                        },
                        onSuccess = { request, result ->
                            Log.d("MentorCard", "Tải hình ảnh thành công: $secureImageUrl")
                        },
                        onError = { request, throwable ->
                            Log.e("MentorCard", "Lỗi khi tải hình ảnh: $secureImageUrl, lỗi: ${throwable.throwable.message}")
                        }
                    )
                    .build(),
                contentDescription = "Mentor Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(id = R.drawable.nguyenquangkinh),
                error = painterResource(id = R.drawable.nguyenquangkinh),
                contentScale = ContentScale.Crop
            )

            Text(
                text = mentor.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 16.sp)
            )

            Text(
                text = mentor.expertise ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )

            Text(
                text = mentor.organization ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )

            Text(
                text = mentor.achievements ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )
        }
    }
}