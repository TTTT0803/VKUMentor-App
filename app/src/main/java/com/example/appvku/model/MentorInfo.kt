package com.example.appvku.model

data class MentorInfo(
    val id: String? = null,
    val name: String = "",
    val expertise: String = "",
    val achievements: String = "",
    val organization: String = "",
    val status: String = "",
    val image: String? = null,  // Đổi thành String? để cho phép null
    val suggestionsQuestions: String? = null,  // Đổi thành String? để cho phép null
    val userId: String = "",
    val referralSource: String = ""
)