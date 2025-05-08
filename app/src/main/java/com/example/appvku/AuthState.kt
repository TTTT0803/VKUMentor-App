package com.example.appvku

import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthState {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var currentUser by mutableStateOf(auth.currentUser)
        private set

    var userRole by mutableStateOf<String?>(null)
        private set

    var isRoleLoading by mutableStateOf(true)
        private set

    init {
        Log.d("AuthState", "Khởi tạo AuthState, kiểm tra trạng thái đăng nhập ban đầu")
        auth.addAuthStateListener { firebaseAuth ->
            Log.d("AuthState", "Trạng thái đăng nhập thay đổi: currentUser = ${firebaseAuth.currentUser?.uid}")
            currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                Log.d("AuthState", "Người dùng đã đăng nhập, lấy vai trò...")
                fetchUserRole()
            } else {
                Log.d("AuthState", "Không có người dùng đăng nhập, đặt userRole = null")
                userRole = null
                isRoleLoading = false
            }
        }
    }

    private fun fetchUserRole() {
        isRoleLoading = true
        currentUser?.let { user ->
            Log.d("AuthState", "Lấy vai trò cho người dùng: UID=${user.uid}, Email=${user.email}")
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("AuthState", "Tài liệu người dùng tồn tại: ${document.data}")
                        val idRole = document.getString("idRole") ?: ""
                        Log.d("AuthState", "idRole = $idRole")
                        if (idRole.isNotEmpty()) {
                            Log.d("AuthState", "Tra cứu vai trò trong collection roles với idRole: $idRole")
                            db.collection("roles").document(idRole).get()
                                .addOnSuccessListener { roleDoc ->
                                    if (roleDoc.exists()) {
                                        userRole = roleDoc.getString("roleName")?.lowercase() ?: "unknown"
                                        Log.d("AuthState", "Lấy được vai trò: $userRole")
                                    } else {
                                        Log.e("AuthState", "Không tìm thấy tài liệu vai trò với idRole: $idRole")
                                        userRole = "unknown"
                                    }
                                    isRoleLoading = false
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("AuthState", "Lỗi khi lấy tài liệu vai trò: ${exception.message}", exception)
                                    userRole = "unknown"
                                    isRoleLoading = false
                                }
                        } else {
                            Log.e("AuthState", "idRole rỗng hoặc không tồn tại trong tài liệu người dùng")
                            userRole = "unknown"
                            isRoleLoading = false
                        }
                    } else {
                        Log.e("AuthState", "Không tìm thấy tài liệu người dùng với UID: ${user.uid}")
                        userRole = "unknown"
                        isRoleLoading = false
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthState", "Lỗi khi lấy tài liệu người dùng: ${exception.message}", exception)
                    userRole = "unknown"
                    isRoleLoading = false
                }
        } ?: run {
            Log.d("AuthState", "Không có currentUser, đặt userRole = null")
            userRole = null
            isRoleLoading = false
        }
    }

    fun signOut() {
        Log.d("AuthState", "Đăng xuất người dùng")
        auth.signOut()
    }
}

val LocalAuthState = compositionLocalOf<AuthState> { error("No AuthState provided") }