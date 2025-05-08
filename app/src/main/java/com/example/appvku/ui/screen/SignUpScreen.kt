package com.example.appvku.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.R
import com.example.appvku.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE5F0FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.login),
                contentDescription = "VKUMentor Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(1.80f)
                    .offset(y = (-50).dp)
            )
            Text(
                text = "Reach Your Goal!",
                fontSize = 28.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-20).dp)
            )

            Text(
                text = "Sign Up",
                fontSize = 28.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                textStyle = TextStyle(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                textStyle = TextStyle(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.length >= 6) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        // Tìm role mentee trong Firestore
                                        db.collection("roles")
                                            .whereEqualTo("roleName", "mentee") // Sửa thành chữ thường để khớp với dữ liệu mẫu
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                if (!documents.isEmpty) {
                                                    val menteeRoleId = documents.documents[0].id
                                                    // Lưu thông tin người dùng vào Firestore với role mentee
                                                    val newUser = hashMapOf(
                                                        "uid" to user.uid,
                                                        "username" to email.split("@")[0],
                                                        "email" to email,
                                                        "idRole" to menteeRoleId,
                                                        "avatar" to "https://res.cloudinary.com/dhku1c1t1/image/upload/v1746449726/NQK_aq8ilm.jpg", // Khớp với dữ liệu mẫu
                                                        "createdAt" to "2025-05-07T10:00:00+07:00" // Khớp với dữ liệu mẫu
                                                    )
                                                    db.collection("users")
                                                        .document(user.uid)
                                                        .set(newUser)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show()
                                                            // Đăng xuất để làm mới trạng thái đăng nhập
                                                            auth.signOut()
                                                            navController.navigate("login") {
                                                                popUpTo(navController.graph.startDestinationId) {
                                                                    inclusive = true
                                                                }
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(context, "Lỗi khi lưu thông tin người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                } else {
                                                    Toast.makeText(context, "Không tìm thấy vai trò mentee trong Firestore", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Lỗi khi tìm vai trò mentee: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Không thể lấy thông tin người dùng sau khi đăng ký", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Đăng ký thất bại: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập email hợp lệ và mật khẩu (tối thiểu 6 ký tự)", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("SIGN UP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text("Đã có tài khoản? Đăng nhập", color = Color.Black)
            }
        }
    }
}