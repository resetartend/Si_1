package com.example.si_1

data class Complaint(
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)