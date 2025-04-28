package com.hackathon.smartdustbin.models

import com.google.firebase.Timestamp

data class Bin(
    val id: String = "",
    val location: String = "",
    val current_fill_level: Double = 0.0,
    val status: String = "active",
    val last_emptied: Timestamp? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) 