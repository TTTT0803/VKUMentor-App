package com.example.appvku.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import com.example.appvku.ui.component.AppBottomBar
import com.example.appvku.ui.component.AppTopBar
import com.example.appvku.ui.component.DrawerContent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authState = LocalAuthState.current
    val snackbarHostState = remember { SnackbarHostState() }

    var visibleMentors by remember { mutableStateOf(listOf<MentorInfo>()) }
    var lastDocument by remember { mutableStateOf<com.google.firebase.firestore.DocumentSnapshot?>(null) }
    var page by remember { mutableStateOf(0) }
    val mentorsPerPage = 9
    val additionalMentors = 6
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gridState = rememberLazyGridState()
    var selectedItem by remember { mutableStateOf("home") }

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