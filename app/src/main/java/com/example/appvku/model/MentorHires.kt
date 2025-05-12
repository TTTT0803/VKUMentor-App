package com.example.appvku.model

data class MentorHires(
    val menteeId: String = "",
    val mentorId: String = "",
    val hireDate: String = ""
) {
    // Constructor không tham số cho Firestore
    constructor() : this("", "", "")
}