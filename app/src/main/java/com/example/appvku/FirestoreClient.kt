package com.example.appvku

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreClient {
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Tên các collections
    const val ROLES_COLLECTION = "roles"
    const val USERS_COLLECTION = "users"
    const val MENTOR_INFO_COLLECTION = "mentor_info"
    const val COMPETITIONS_COLLECTION = "competitions"
    const val MENTOR_MENTEE_LIST_COLLECTION = "mentor_mentee_list"
    const val COMPETITIONS_REGISTER_COLLECTION = "competitions_register"
    const val MENTOR_RATING_COLLECTION = "mentor_rating"
    const val UNIVERSITY_PARTNERS_COLLECTION = "university_partners"
    const val COMMUNITY_DOCUMENTS_COLLECTION = "community_documents"
    const val COMMENTS_COLLECTION = "comments"
    const val SLIDER_IMAGES_COLLECTION = "slider_images"
    const val ADMIN_COLLECTION = "admin"
    const val MENTEE_INFO_COLLECTION = "mentee_info"
    const val CONTACT_MESSAGES_COLLECTION = "contact_messages"
}