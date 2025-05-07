package com.example.appvku

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirestoreDataManager {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreDataManager"

    fun deleteAllData(onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val collections = listOf(
                    "users",
                    "community_documents",
                    "mentor_info",
                    "roles"
                )

                for (collection in collections) {
                    deleteCollection(collection, 500)
                    Log.d(TAG, "Đã xóa collection: $collection")
                }
                Log.d(TAG, "Xóa toàn bộ dữ liệu Firestore thành công")
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi xóa dữ liệu: ${e.message}", e)
            }
        }
    }

    private suspend fun deleteCollection(collectionPath: String, batchSize: Int) {
        var query = db.collection(collectionPath).limit(batchSize.toLong())
        var deleted = true

        while (deleted) {
            val snapshot: QuerySnapshot = query.get().await()
            val batch = db.batch()
            deleted = false

            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
                deleted = true
            }

            batch.commit().await()

            if (snapshot.size() >= batchSize) {
                query = db.collection(collectionPath)
                    .limit(batchSize.toLong())
                    .startAfter(snapshot.documents[snapshot.size() - 1])
            }
        }
    }
}