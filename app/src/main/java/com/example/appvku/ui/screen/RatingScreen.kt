package com.example.appvku.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import com.example.appvku.model.MentorRating
import com.example.appvku.FirestoreClient
import com.example.appvku.ui.component.AppBottomBar
import com.example.appvku.ui.component.AppTopBar
import com.example.appvku.ui.component.DrawerContent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authState = LocalAuthState.current
    val snackbarHostState = remember { SnackbarHostState() }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var hiredMentors by remember { mutableStateOf<List<MentorInfo>>(emptyList()) }
    var selectedMentorId by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Kiểm tra nếu người dùng chưa đăng nhập
    LaunchedEffect(authState.currentUser) {
        if (authState.currentUser == null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Vui lòng đăng nhập để đánh giá!",
                    actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
                navController.navigate("login")
            }
        } else {
            // Lấy danh sách mentor đã thuê
            db.collection(FirestoreClient.MENTOR_HIRES_COLLECTION)
                .whereEqualTo("menteeId", authState.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val mentorIds = documents.map { it.getString("mentorId")!! }
                    if (mentorIds.isNotEmpty()) {
                        db.collection(FirestoreClient.MENTOR_INFO_COLLECTION)
                            .whereIn("id", mentorIds)
                            .get()
                            .addOnSuccessListener { mentorDocs ->
                                hiredMentors = mentorDocs.map { doc ->
                                    doc.toObject(MentorInfo::class.java).copy(id = doc.id)
                                }
                                Log.d("RatingScreen", "Đã lấy được ${hiredMentors.size} mentor đã thuê")
                            }
                            .addOnFailureListener { exception ->
                                errorMessage = "Lỗi khi tải danh sách mentor: ${exception.message}"
                                Log.e("RatingScreen", "Lỗi tải mentor: ${exception.message}")
                            }
                    } else {
                        errorMessage = "Bạn chưa thuê mentor nào để đánh giá!"
                        Log.d("RatingScreen", "Người dùng chưa thuê mentor nào")
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Lỗi khi tải danh sách thuê: ${exception.message}"
                    Log.e("RatingScreen", "Lỗi tải danh sách thuê: ${exception.message}")
                }
        }
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
                    selectedItem = "rating"
                )
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5F0FF))
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đánh giá Mentor",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "Đã có lỗi xảy ra",
                                color = Color.Red,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else if (hiredMentors.isEmpty()) {
                            Text(
                                text = "Bạn chưa thuê mentor nào để đánh giá!",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Hiển thị danh sách mentor đã thuê
                            Text(
                                text = "Chọn mentor để đánh giá:",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(256.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                items(hiredMentors.size) { index ->
                                    val mentor = hiredMentors[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedMentorId = mentor.id }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(mentor.image?.replace("http://", "https://") ?: R.drawable.nguyenquangkinh)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Hình ảnh Mentor",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape),
                                            placeholder = painterResource(id = R.drawable.nguyenquangkinh),
                                            error = painterResource(id = R.drawable.nguyenquangkinh),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = mentor.name,
                                            fontSize = 16.sp,
                                            color = if (selectedMentorId == mentor.id) Color(0xFF2961B4) else Color.Black
                                        )
                                    }
                                }
                            }

                            if (selectedMentorId != null) {
                                // Chọn điểm số
                                Text(
                                    text = "Chọn số sao (1-5):",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly // Sử dụng SpaceEvenly để phân bố đều
                                ) {
                                    for (i in 1..5) {
                                        IconButton(
                                            onClick = { rating = i },
                                            modifier = Modifier.size(48.dp) // Giảm kích thước để giao diện gọn hơn
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_star),
                                                contentDescription = "$i sao",
                                                tint = if (i <= rating) Color(0xFFFFD700) else Color(0xFF888888), // Vàng đậm và xám nhạt
                                                modifier = Modifier.size(32.dp) // Kích thước ngôi sao hợp lý
                                            )
                                        }
                                    }
                                }

                                // Nhập bình luận
                                TextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Bình luận") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(8.dp)), // Bo góc cho TextField
                                    colors = TextFieldDefaults.textFieldColors(
                                        containerColor = Color.White,
                                        focusedIndicatorColor = Color(0xFF2961B4),
                                        unfocusedIndicatorColor = Color.Gray
                                    )
                                )

                                // Nút gửi đánh giá
                                Button(
                                    onClick = {
                                        if (rating > 0) {
                                            isLoading = true
                                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val currentDate = dateFormat.format(Date())
                                            val newRating = MentorRating(
                                                mentorId = selectedMentorId!!,
                                                userId = authState.currentUser!!.uid,
                                                rating = rating,
                                                comment = comment.text,
                                                date = currentDate
                                            )
                                            db.collection(FirestoreClient.MENTOR_RATING_COLLECTION)
                                                .add(newRating)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    // Loại bỏ mentor vừa được đánh giá khỏi danh sách
                                                    hiredMentors = hiredMentors.filter { it.id != selectedMentorId }
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Đánh giá đã được gửi!",
                                                            actionLabel = "OK",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                    comment = TextFieldValue("")
                                                    rating = 0
                                                    selectedMentorId = null
                                                }
                                                .addOnFailureListener { exception ->
                                                    isLoading = false
                                                    errorMessage = "Lỗi khi gửi đánh giá: ${exception.message}"
                                                    Log.e("RatingScreen", "Lỗi gửi đánh giá: ${exception.message}", exception)
                                                }
                                        } else {
                                            errorMessage = "Vui lòng chọn số sao!"
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isLoading // Vô hiệu hóa nút khi đang tải
                                ) {
                                    Text("Gửi đánh giá", color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}