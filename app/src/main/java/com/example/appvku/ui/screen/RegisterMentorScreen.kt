package com.example.appvku.ui.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appvku.AuthManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMentorScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var achievements by remember { mutableStateOf("") }
    var expertise by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var referralSource by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lấy username từ Firestore
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: "Unknown"
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterMentorScreen", "Lỗi khi lấy username: ${e.message}")
                    username = "Unknown"
                }
        }
    }

    // Launcher để chọn hình ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            isLoading = true
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("RegisterMentorScreen", "Bắt đầu upload ảnh lên Cloudinary")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        Log.d("RegisterMentorScreen", "Đang upload: $bytes/$totalBytes")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        imageUrl = resultData["url"].toString()
                        Log.d("RegisterMentorScreen", "Upload thành công, URL: $imageUrl")
                        isLoading = false
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("RegisterMentorScreen", "Lỗi khi upload: ${error.description}")
                        errorMessage = "Lỗi khi upload ảnh: ${error.description}"
                        isLoading = false
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("RegisterMentorScreen", "Đang thử upload lại: ${error.description}")
                    }
                })
                .dispatch()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5F0FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Đăng ký Mentor",
                fontSize = 28.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = achievements,
                onValueChange = { achievements = it },
                label = { Text("Thành tựu") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = expertise,
                onValueChange = { expertise = it },
                label = { Text("Chuyên môn") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = organization,
                onValueChange = { organization = it },
                label = { Text("Tổ chức") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = referralSource,
                onValueChange = { referralSource = it },
                label = { Text("Nguồn giới thiệu") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị hình ảnh
            when {
                imageUrl != null -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Hình ảnh từ Cloudinary",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                imageUri != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Hình ảnh đã chọn",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                else -> {
                    Text(
                        text = "Chưa có hình ảnh được chọn",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                Text(
                    "Chọn hình ảnh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (imageUrl == null) {
                        errorMessage = "Vui lòng chọn và upload hình ảnh trước khi đăng ký."
                        return@Button
                    }
                    val currentUser = auth.currentUser
                    if (currentUser != null && achievements.isNotBlank() && expertise.isNotBlank() && organization.isNotBlank()) {
                        isLoading = true
                        Log.d("RegisterMentorScreen", "Chuẩn bị lưu dữ liệu vào Firestore với imageUrl: $imageUrl")
                        db.collection("users")
                            .document(currentUser.uid)
                            .update("role", "Mentor")
                            .addOnSuccessListener {
                                val mentorInfo = MentorInfo(
                                    id = currentUser.uid,
                                    achievements = achievements,
                                    expertise = expertise,
                                    organization = organization,
                                    referralSource = referralSource,
                                    status = "pending",
                                    image = imageUrl,
                                    userId = currentUser.uid,
                                    name = username,
                                    suggestionsQuestions = null
                                )
                                db.collection("mentor_info")
                                    .document(currentUser.uid)
                                    .set(mentorInfo)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Đăng ký mentor thành công! Vui lòng chờ admin duyệt.", Toast.LENGTH_SHORT).show()
                                        AuthManager.fetchUserRole(currentUser.uid) { role ->
                                            if (role == "admin") {
                                                navController.navigate("pending_approval") {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                navController.navigate("home") {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                        isLoading = false
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Lỗi khi lưu thông tin mentor: ${e.message}"
                                        isLoading = false
                                    }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Lỗi khi cập nhật vai trò: ${e.message}"
                                isLoading = false
                            }
                    } else {
                        errorMessage = "Vui lòng điền đầy đủ thông tin và chọn hình ảnh."
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && imageUrl != null
            ) {
                Text(
                    "ĐĂNG KÝ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}