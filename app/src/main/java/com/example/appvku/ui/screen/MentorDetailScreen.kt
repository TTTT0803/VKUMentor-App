package com.example.appvku.ui.screen

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDetailScreen(navController: NavHostController, mentorId: String) {
    val db = FirebaseFirestore.getInstance()
    val authState = LocalAuthState.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var mentorInfo by remember { mutableStateOf<MentorInfo?>(null) }
    var ratingsWithUserNames by remember { mutableStateOf<List<Pair<MentorRating, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasHired by remember { mutableStateOf(false) }

    // Lấy thông tin mentor và đánh giá từ Firestore
    LaunchedEffect(mentorId) {
        // Lấy thông tin mentor
        db.collection(FirestoreClient.MENTOR_INFO_COLLECTION).document(mentorId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    mentorInfo = document.toObject(MentorInfo::class.java)?.copy(id = document.id)
                    Log.d("MentorDetailScreen", "Lấy thông tin mentor thành công: $mentorInfo")
                } else {
                    errorMessage = "Không tìm thấy thông tin mentor."
                    Log.e("MentorDetailScreen", "Không tìm thấy mentor với ID: $mentorId")
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Lỗi khi tải thông tin mentor: ${exception.message}"
                Log.e("MentorDetailScreen", "Lỗi khi tải mentor: ${exception.message}", exception)
                isLoading = false
            }

        // Lấy danh sách đánh giá từ Firestore và tên người đánh giá
        scope.launch {
            try {
                val documents = db.collection(FirestoreClient.MENTOR_RATING_COLLECTION)
                    .whereEqualTo("mentorId", mentorId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ratingsList = mutableListOf<Pair<MentorRating, String>>()
                for (doc in documents) {
                    val rating = doc.toObject(MentorRating::class.java).copy(id = doc.id)
                    // Lấy tên người dùng từ collection users dựa trên userId
                    val userDoc = db.collection(FirestoreClient.USERS_COLLECTION)
                        .document(rating.userId)
                        .get()
                        .await()
                    val username = userDoc.getString("username") ?: "Người dùng không xác định"
                    ratingsList.add(Pair(rating, username))
                }
                ratingsWithUserNames = ratingsList
                Log.d("MentorDetailScreen", "Lấy được ${ratingsList.size} đánh giá cho mentor $mentorId")
            } catch (exception: Exception) {
                Log.e("MentorDetailScreen", "Lỗi khi tải đánh giá: ${exception.message}", exception)
            }
        }

        // Kiểm tra xem người dùng đã thuê mentor này chưa
        if (authState.currentUser != null) {
            db.collection(FirestoreClient.MENTOR_HIRES_COLLECTION)
                .whereEqualTo("mentorId", mentorId)
                .whereEqualTo("menteeId", authState.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    hasHired = !documents.isEmpty
                    Log.d("MentorDetailScreen", "Người dùng đã thuê mentor: $hasHired")
                }
                .addOnFailureListener { exception ->
                    Log.e("MentorDetailScreen", "Lỗi khi kiểm tra trạng thái thuê: ${exception.message}")
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Thông tin Mentor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Quay lại",
                            modifier = Modifier.size(96.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE5F0FF))
            )
        },
        content = { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Đã có lỗi xảy ra",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            } else if (mentorInfo != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5F0FF))
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        // Hiển thị thông tin mentor
                        MentorDetailHeader(mentorInfo!!)
                        Spacer(modifier = Modifier.height(16.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tiêu đề "Đánh giá từ Mentee" và nút "Thuê"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đánh giá từ Mentee",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Nút "Thuê" được hiển thị cho cả mentor và mentee
                            Button(
                                onClick = {
                                    if (authState.currentUser == null) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Vui lòng đăng nhập để thuê mentor!",
                                                actionLabel = "OK",
                                                duration = SnackbarDuration.Short
                                            )
                                            navController.navigate("login")
                                        }
                                    } else if (hasHired) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bạn đã thuê mentor này rồi!",
                                                actionLabel = "OK",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        val hireData = hashMapOf(
                                            "menteeId" to authState.currentUser!!.uid,
                                            "mentorId" to mentorId,
                                            "hireDate" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault()).format(java.util.Date())
                                        )
                                        db.collection(FirestoreClient.MENTOR_HIRES_COLLECTION)
                                            .document("${authState.currentUser!!.uid}_$mentorId")
                                            .set(hireData)
                                            .addOnSuccessListener {
                                                hasHired = true
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Đã thuê mentor thành công!",
                                                        actionLabel = "OK",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Lỗi khi thuê mentor: ${exception.message}",
                                                        actionLabel = "OK",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                    }
                                },
                                modifier = Modifier
                                    .height(40.dp)
                                    .padding(bottom = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Thuê",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (ratingsWithUserNames.isEmpty()) {
                            Text(
                                text = "Chưa có đánh giá nào.",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            ratingsWithUserNames.forEach { (rating, username) ->
                                RatingCard(rating, username)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MentorDetailHeader(mentor: MentorInfo) {
    val context = LocalContext.current
    val secureImageUrl = mentor.image?.replace("http://", "https://") ?: R.drawable.nguyenquangkinh

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(secureImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Hình ảnh Mentor",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            placeholder = painterResource(id = R.drawable.nguyenquangkinh),
            error = painterResource(id = R.drawable.nguyenquangkinh),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = mentor.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chuyên môn: ${mentor.expertise ?: "Chưa có thông tin"}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(lineHeight = 16.sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tổ chức: ${mentor.organization ?: "Chưa có thông tin"}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(lineHeight = 16.sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Thành tựu: ${mentor.achievements ?: "Chưa có thông tin"}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(lineHeight = 16.sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Nguồn giới thiệu: ${mentor.referralSource ?: "Chưa có thông tin"}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(lineHeight = 16.sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Câu hỏi gợi ý: ${mentor.suggestionsQuestions ?: "Chưa có thông tin"}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(lineHeight = 16.sp)
        )
    }
}

@Composable
fun RatingCard(rating: MentorRating, username: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$username",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đánh giá: ${rating.rating}/5",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Text(
                    text = rating.date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rating.comment ?: "Không có bình luận",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}