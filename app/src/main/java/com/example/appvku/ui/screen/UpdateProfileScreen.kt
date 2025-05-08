package com.example.appvku.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(navController: NavHostController) {
    val authState = LocalAuthState.current
    val db = FirebaseFirestore.getInstance()

    var mentorInfo by remember { mutableStateOf(MentorInfo()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lấy thông tin từ Firestore (collection mentor_info)
    LaunchedEffect(authState.currentUser?.uid) {
        isLoading = true
        errorMessage = null
        authState.currentUser?.uid?.let { uid ->
            db.collection("mentor_info")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty()) {
                        Log.w("UpdateProfileScreen", "Không tìm thấy tài liệu cho userId: $uid trong mentor_info")
                        mentorInfo = MentorInfo(userId = uid)
                        errorMessage = "Đăng ký làm Mentor để được cập nhật hồ sơ"
                    } else {
                        val document = documents.documents.first()
                        mentorInfo = document.toObject(MentorInfo::class.java)?.copy(id = document.id) ?: MentorInfo(userId = uid)
                        Log.d("UpdateProfileScreen", "Dữ liệu tải thành công: $mentorInfo, image URL: ${mentorInfo.image}")
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateProfileScreen", "Lỗi tải thông tin: ${e.message}")
                    errorMessage = "Lỗi tải thông tin: ${e.message}"
                    isLoading = false
                }
        } ?: run {
            Log.w("UpdateProfileScreen", "UID của người dùng hiện tại là null")
            errorMessage = "Người dùng chưa đăng nhập"
            isLoading = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("UpdateProfileScreen", "Bắt đầu upload ảnh lên Cloudinary")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val newImageUrl = resultData["secure_url"]?.toString() ?: resultData["url"]?.toString()
                        Log.d("UpdateProfileScreen", "Upload thành công, URL: $newImageUrl")
                        mentorInfo = mentorInfo.copy(image = newImageUrl)
                        authState.currentUser?.uid?.let { uid ->
                            db.collection("mentor_info")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        val docId = documents.documents.first().id
                                        db.collection("mentor_info").document(docId)
                                            .update("image", newImageUrl)
                                            .addOnSuccessListener {
                                                Log.d("UpdateProfileScreen", "Cập nhật URL ảnh thành công cho userId: $uid")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("UpdateProfileScreen", "Lỗi cập nhật URL ảnh cho userId: $uid - ${e.message}")
                                            }
                                    }
                                }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("UpdateProfileScreen", "Lỗi khi upload: ${error.description}")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cập nhật Hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Hiển thị hình ảnh và nút camera trong Row để căn chỉnh tốt hơn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = mentorInfo.image ?: "",
                        contentDescription = "Hình ảnh Hồ sơ",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        placeholder = painterResource(id = R.drawable.taikhoan),
                        error = painterResource(id = R.drawable.taikhoan)
                    )
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Thay đổi Hình ảnh",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Thay đổi Hình ảnh Hồ sơ",
                    fontSize = 14.sp,
                    color = Color(0xFF2961B4),
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = mentorInfo.name,
                    onValueChange = { mentorInfo = mentorInfo.copy(name = it) },
                    label = { Text("Tên") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = mentorInfo.achievements,
                    onValueChange = { mentorInfo = mentorInfo.copy(achievements = it) },
                    label = { Text("Thành tựu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = mentorInfo.expertise,
                    onValueChange = { mentorInfo = mentorInfo.copy(expertise = it) },
                    label = { Text("Chuyên môn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = mentorInfo.organization,
                    onValueChange = { mentorInfo = mentorInfo.copy(organization = it) },
                    label = { Text("Tổ chức") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = mentorInfo.referralSource,
                    onValueChange = { mentorInfo = mentorInfo.copy(referralSource = it) },
                    label = { Text("Nguồn giới thiệu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = mentorInfo.suggestionsQuestions ?: "",
                    onValueChange = { mentorInfo = mentorInfo.copy(suggestionsQuestions = it) },
                    label = { Text("Gợi ý/Câu hỏi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Button(
                    onClick = {
                        authState.currentUser?.uid?.let { uid ->
                            val updatedMentorInfo = mentorInfo.copy(userId = uid)
                            Log.d("UpdateProfileScreen", "Dữ liệu gửi lên Firestore: $updatedMentorInfo")
                            db.collection("mentor_info")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty()) {
                                        db.collection("mentor_info")
                                            .add(updatedMentorInfo)
                                            .addOnSuccessListener { docRef ->
                                                mentorInfo = updatedMentorInfo.copy(id = docRef.id)
                                                Log.d("UpdateProfileScreen", "Tạo và cập nhật thông tin thành công cho userId: $uid, docId: ${docRef.id}")
                                                navController.popBackStack()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("UpdateProfileScreen", "Lỗi tạo tài liệu cho userId: $uid - ${e.message}")
                                            }
                                    } else {
                                        val docId = documents.documents.first().id
                                        db.collection("mentor_info").document(docId)
                                            .set(updatedMentorInfo.copy(id = docId))
                                            .addOnSuccessListener {
                                                Log.d("UpdateProfileScreen", "Cập nhật thông tin thành công cho userId: $uid, docId: $docId")
                                                navController.popBackStack()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("UpdateProfileScreen", "Lỗi cập nhật thông tin cho userId: $uid - ${e.message}")
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UpdateProfileScreen", "Lỗi kiểm tra tài liệu cho userId: $uid - ${e.message}")
                                }
                        } ?: run {
                            Log.e("UpdateProfileScreen", "Không thể cập nhật: UID là null")
                        }
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cập nhật Thông tin", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Cập nhật",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}