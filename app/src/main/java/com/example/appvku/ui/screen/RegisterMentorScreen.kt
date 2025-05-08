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
import coil.compose.rememberAsyncImagePainter
import com.example.appvku.model.MentorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMentorScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var expertise by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var achievements by remember { mutableStateOf("") }
    var referralSource by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên") },
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
                value = achievements,
                onValueChange = { achievements = it },
                label = { Text("Thành tựu") },
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

            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Hình ảnh đã chọn",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text(
                    text = "Chưa có hình ảnh được chọn",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                    if (name.isNotEmpty() && expertise.isNotEmpty() && organization.isNotEmpty() &&
                        achievements.isNotEmpty() && referralSource.isNotEmpty() && imageUrl.isNotEmpty()
                    ) {
                        isLoading = true
                        val user = auth.currentUser
                        if (user != null) {
                            // Tạo tài liệu trong mentor_info
                            val mentorInfo = MentorInfo(
                                id = null, // Sẽ được tự động gán bởi Firestore
                                userId = user.uid,
                                name = name,
                                expertise = expertise,
                                organization = organization,
                                achievements = achievements,
                                referralSource = referralSource,
                                image = imageUrl,
                                status = "pending",
                                suggestionsQuestions = null
                            )

                            db.collection("mentor_info")
                                .add(mentorInfo)
                                .addOnSuccessListener { documentReference ->
                                    // Cập nhật ID của tài liệu vừa tạo
                                    documentReference.update("id", documentReference.id)
                                    Toast.makeText(context, "Đăng ký Mentor thành công! Đang chờ phê duyệt.", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                    isLoading = false
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Lỗi khi đăng ký Mentor: ${e.message}"
                                    isLoading = false
                                }
                        } else {
                            Toast.makeText(context, "Vui lòng đăng nhập để đăng ký Mentor!", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn hình ảnh!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
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