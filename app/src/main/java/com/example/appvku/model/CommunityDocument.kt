package com.example.appvku.model

data class CommunityDocument(
    val id: String = "",
    val title: String = "",
    val content: String? = null,
    val date: String = "",
    val image: String? = null,
    val mentorId: String = "",
    val fileUrl: String? = null // New field for PDF URL
)