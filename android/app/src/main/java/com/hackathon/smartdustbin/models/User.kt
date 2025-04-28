package com.hackathon.smartdustbin.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val total_points: Long = 0,
    val used_points: Long = 0,
    val rank: String = "Novice Recycler",
    val unreadRewards: Int = 0,
    val created_at: com.google.firebase.Timestamp? = null
)
