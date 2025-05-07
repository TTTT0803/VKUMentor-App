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
fun EditMentorScreen(
    navController: NavHostController,
    mentorId: String?,
    name: String,
    expertise: String,
    organization: String,
    achievements: String,
    referralSource: String,
    image: String
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var editedName by remember { mutableStateOf(name) }
    var editedExpertise by remember { mutableStateOf(expertise) }
    var editedOrganization by remember { mutableStateOf(organization) }
    var editedAchievements by remember { mutableStateOf(achievements) }
    var editedReferralSource by remember { mutableStateOf(referralSource) }
    var editedImageUrl by remember { mutableStateOf(image) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher để chọn hình ảnh mới
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            isLoading = true
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("EditMentorScreen", "Bắt đầu upload ảnh lên Cloudinary")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        Log.d("EditMentorScreen", "Đang upload: $bytes/$totalBytes")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        editedImageUrl = resultData["url"].toString()
                        Log.d("EditMentorScreen", "Upload thành công, URL: $editedImageUrl")
                        isLoading = false
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("EditMentorScreen", "Lỗi khi upload: ${error.description}")
                        errorMessage = "Lỗi khi upload ảnh: ${error.description}"
                        isLoading = false
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("EditMentorScreen", "Đang thử upload lại: ${error.description}")
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
                text = "Chỉnh sửa Mentor",
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
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Tên") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedExpertise,
                onValueChange = { editedExpertise = it },
                label = { Text("Chuyên môn") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedOrganization,
                onValueChange = { editedOrganization = it },
                label = { Text("Tổ chức") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedAchievements,
                onValueChange = { editedAchievements = it },
                label = { Text("Thành tựu") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedReferralSource,
                onValueChange = { editedReferralSource = it },
                label = { Text("Nguồn giới thiệu") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị hình ảnh
            when {
                editedImageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model = editedImageUrl,
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
                    "Chọn hình ảnh mới",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (mentorId != null) {
                        isLoading = true
                        val updatedMentor = MentorInfo(
                            id = mentorId,
                            name = editedName,
                            expertise = editedExpertise,
                            organization = editedOrganization,
                            achievements = editedAchievements,
                            referralSource = editedReferralSource,
                            status = "pending",
                            image = editedImageUrl,
                            userId = mentorId,
                            suggestionsQuestions = null
                        )
                        db.collection("mentor_info")
                            .document(mentorId)
                            .set(updatedMentor)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Chỉnh sửa thông tin thành công!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                isLoading = false
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Lỗi khi cập nhật thông tin: ${e.message}"
                                isLoading = false
                            }
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
                    "LƯU THAY ĐỔI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}