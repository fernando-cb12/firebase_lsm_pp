package com.example.firebase_lsm_pp.auth

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val points: Int = 0,
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)