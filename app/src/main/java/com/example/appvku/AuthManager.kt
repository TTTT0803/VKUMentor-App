package com.example.appvku

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        Log.d("AuthManager", "Attempting sign-in with email: '$email', password: '$password'")
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        fetchUserRole(user.uid) { role ->
                            callback(true, role)
                        }
                    } else {
                        callback(false, null)
                    }
                } else {
                    Log.e("AuthManager", "Sign-in failed: ${task.exception?.message}")
                    callback(false, null)
                }
            }
    }

    fun fetchUserRole(uid: String, callback: (String?) -> Unit) {
        // Giả định lấy role từ Firestore (thay bằng logic thực tế của bạn)
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                callback(role)
            }
            .addOnFailureListener { exception ->
                Log.e("AuthManager", "Failed to fetch role: ${exception.message}")
                callback(null)
            }
    }

    fun signOut() {
        auth.signOut()
    }
}