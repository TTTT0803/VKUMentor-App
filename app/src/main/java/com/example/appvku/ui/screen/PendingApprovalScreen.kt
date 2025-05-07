package com.example.appvku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.model.MentorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var pendingMentors by remember { mutableStateOf(listOf<MentorInfo>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tải danh sách Mentor có status = "pending"
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        db.collection("mentor_info")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    errorMessage = "Không có Mentor nào đang chờ duyệt."
                } else {
                    val mentors = documents.map { doc ->
                        val mentor = doc.toObject(MentorInfo::class.java)
                        mentor.copy(id = doc.id)
                    }
                    pendingMentors = mentors
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Lỗi khi tải dữ liệu: ${exception.message}"
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5F0FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Duyệt Mentor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng xuất thứ nhất (mới thêm)
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("splash") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pendingMentors.size) { index ->
                        val mentor = pendingMentors[index]
                        MentorApprovalCard(
                            mentor = mentor,
                            onApprove = {
                                db.collection("mentor_info")
                                    .document(mentor.id ?: "")
                                    .update("status", "approved")
                                    .addOnSuccessListener {
                                        pendingMentors = pendingMentors.filter { it.id != mentor.id }
                                    }
                            },
                            onReject = {
                                db.collection("mentor_info")
                                    .document(mentor.id ?: "")
                                    .update("status", "rejected")
                                    .addOnSuccessListener {
                                        pendingMentors = pendingMentors.filter { it.id != mentor.id }
                                    }
                            },
                            onEdit = {
                                navController.navigate("edit_mentor/${mentor.id}/${mentor.name}/${mentor.expertise}/${mentor.organization}/${mentor.achievements}/${mentor.referralSource}/${mentor.image}")
                            },
                            onDelete = {
                                db.collection("mentor_info")
                                    .document(mentor.id ?: "")
                                    .delete()
                                    .addOnSuccessListener {
                                        pendingMentors = pendingMentors.filter { it.id != mentor.id }
                                    }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút Đăng xuất thứ hai (nút cũ)
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("splash") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MentorApprovalCard(
    mentor: MentorInfo,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Tên: ${mentor.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chuyên môn: ${mentor.expertise ?: "Chưa có thông tin"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tổ chức: ${mentor.organization ?: "Chưa có thông tin"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Thành tựu: ${mentor.achievements ?: "Chưa có thông tin"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Phê duyệt", color = Color.White)
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Chỉnh sửa", color = Color.White)
                }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Từ chối", color = Color.White)
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xóa", color = Color.White)
                }
            }
        }
    }
}