package com.example.si_1.model

data class User(
    val uid: String = "",
    val email: String = "",
    val gender: String = "",     // "남성" 또는 "여성"
    val ageGroup: String = "",   // 예: "20대", "30대"
    val isAdmin: Boolean = false
)