package com.hackathon.smartdustbin.models

import com.google.firebase.Timestamp

data class Deposit(
    val user_id: String = "",
    val bin_id: String = "",
    val waste_type: String = "",
    val weight: Double = 0.0,
    val points_earned: Long = 0,
    val timestamp: Timestamp? = null
)

