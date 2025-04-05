package com.example.appvku.model

data class MentorRating(
    val id: String = "",
    val mentorId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val comment: String? = null,
    val date: String = ""
)