package com.example.appvku.model

data class CommunityDocument(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val image:  String? = null,
    val mentorId: String = ""
)