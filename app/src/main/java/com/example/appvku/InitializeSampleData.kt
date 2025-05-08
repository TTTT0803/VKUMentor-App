package com.example.appvku

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

object SampleDataInitializer {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "SampleDataInitializer"

    fun initializeSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Xóa dữ liệu hiện tại trong Firestore
                deleteAllFirestoreData()

                // Tạo roles trước
                initializeRoles()

                // Tạo 14 user trong Authentication và Firestore
                initializeUsers()

                // Khởi tạo dữ liệu cho collection "community_documents"
                initializeCommunityDocuments()

                Log.d(TAG, "Khởi tạo dữ liệu mẫu thành công")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi khởi tạo dữ liệu mẫu: ${e.message}", e)
            }
        }
    }

    private suspend fun deleteAllFirestoreData() {
        val collections = listOf("users", "mentor_info", "community_documents", "roles")
        for (collection in collections) {
            val documents = db.collection(collection).get().await()
            for (document in documents) {
                db.collection(collection).document(document.id).delete().await()
            }
        }
        Log.d(TAG, "Đã xóa toàn bộ dữ liệu trong Firestore")
    }

    private suspend fun initializeRoles() {
        val roles = listOf(
            hashMapOf("roleName" to "admin") to "role_admin",
            hashMapOf("roleName" to "mentee") to "role_mentee",
            hashMapOf("roleName" to "mentor") to "role_mentor"
        )

        for ((roleData, roleId) in roles) {
            db.collection("roles").document(roleId).set(roleData).await()
            Log.d(TAG, "Đã tạo vai trò: $roleId với roleName: ${roleData["roleName"]}")
        }
    }

    private suspend fun initializeUsers() {
        val avatarUrl = "https://res.cloudinary.com/dhku1c1t1/image/upload/v1746449726/NQK_aq8ilm.jpg"
        val createdAt = "2025-05-07T10:00:00+07:00"

        // Tạo 2 Admin
        for (i in 1..2) {
            val adminId = "admin_$i"
            val email = "admin$i@example.com"
            val password = "AdminPass$i"
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    val admin = hashMapOf(
                        "uid" to user.uid,
                        "username" to "Admin $i",
                        "email" to email,
                        "idRole" to "role_admin",
                        "avatar" to avatarUrl,
                        "createdAt" to createdAt
                    )
                    db.collection("users").document(user.uid).set(admin).await()
                    Log.d(TAG, "Đã tạo Admin $i: UID=${user.uid}, Email=$email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi tạo Admin $i: ${e.message}")
            }
        }

        // Tạo 3 Mentee
        for (i in 1..3) {
            val menteeId = "mentee_$i"
            val email = "mentee$i@example.com"
            val password = "MenteePass$i"
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    val mentee = hashMapOf(
                        "uid" to user.uid,
                        "username" to "Mentee $i",
                        "email" to email,
                        "idRole" to "role_mentee",
                        "avatar" to avatarUrl,
                        "createdAt" to createdAt
                    )
                    db.collection("users").document(user.uid).set(mentee).await()
                    Log.d(TAG, "Đã tạo Mentee $i: UID=${user.uid}, Email=$email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi tạo Mentee $i: ${e.message}")
            }
        }

        // Tạo 9 Mentor (6 approved, 3 pending)
        for (i in 1..9) {
            val mentorId = "mentor_$i"
            val email = "mentor$i@example.com"
            val password = "MentorPass$i"
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    val status = if (i <= 6) "approved" else "pending"
                    // Lưu thông tin user
                    val mentor = hashMapOf(
                        "uid" to user.uid,
                        "username" to "Mentor $i",
                        "email" to email,
                        "idRole" to "role_mentor",
                        "avatar" to avatarUrl,
                        "createdAt" to createdAt
                    )
                    db.collection("users").document(user.uid).set(mentor).await()

                    // Lưu thông tin mentor_info
                    val mentorInfo = hashMapOf(
                        "id" to user.uid,
                        "name" to "Mentor $i",
                        "expertise" to "Chuyên môn $i",
                        "achievements" to "Thành tựu $i",
                        "organization" to "Tổ chức $i",
                        "status" to status,
                        "image" to avatarUrl,
                        "suggestionsQuestions" to null,
                        "userId" to user.uid,
                        "referralSource" to "Nguồn $i"
                    )
                    db.collection("mentor_info").document(user.uid).set(mentorInfo).await()
                    Log.d(TAG, "Đã tạo Mentor $i: UID=${user.uid}, Email=$email, Status=$status")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi tạo Mentor $i: ${e.message}")
            }
        }

        Log.d(TAG, "Đã khởi tạo users: 2 Admin, 3 Mentee, 9 Mentor (6 approved, 3 pending)")
    }

    private suspend fun initializeCommunityDocuments() {
        val imageUrl = "https://res.cloudinary.com/dqs4tuaru/image/upload/v1746598543/reactjs_jshk0a.jpg"
        val titles = listOf(
            "Học lập trình Android cơ bản",
            "Hướng dẫn sử dụng Firebase",
            "Tối ưu hóa với Jetpack Compose",
            "Debug hiệu quả trong Android Studio",
            "Sử dụng Kotlin Coroutines",
            "Tích hợp Cloudinary",
            "Phát triển ứng dụng đa nền tảng",
            "Quản lý trạng thái với ViewModel",
            "Thiết kế UI với Material Design"
        )
        val contents = listOf(
            "Hướng dẫn cơ bản lập trình Android cho người mới bắt đầu.",
            "Tìm hiểu cách sử dụng Firebase để lưu trữ dữ liệu.",
            "Cách tối ưu hóa giao diện với Jetpack Compose.",
            "Mẹo debug ứng dụng Android nhanh chóng.",
            "Sử dụng Coroutines để quản lý tác vụ bất đồng bộ.",
            "Hướng dẫn tích hợp Cloudinary vào ứng dụng.",
            "Phát triển ứng dụng trên nhiều nền tảng với Flutter.",
            "Quản lý trạng thái ứng dụng bằng ViewModel.",
            "Thiết kế giao diện đẹp với Material Design."
        )

        for (i in 1..9) {
            val postId = "post_$i"
            val mentorIndex = (i % 9) + 1
            val mentorId = if (mentorIndex <= 9) "mentor_$mentorIndex" else "mentor_1"
            val post = hashMapOf(
                "title" to titles[(i - 1) % titles.size],
                "content" to contents[(i - 1) % contents.size],
                "date" to "2025-05-07T${String.format("%02d", i)}:00:00+07:00",
                "image" to imageUrl,
                "mentorId" to mentorId
            )
            db.collection("community_documents").document(postId).set(post).await()
        }

        Log.d(TAG, "Đã khởi tạo 9 tài liệu cộng đồng")
    }
}