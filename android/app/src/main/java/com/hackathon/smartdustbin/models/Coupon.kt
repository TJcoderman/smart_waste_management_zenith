package com.hackathon.smartdustbin.models

import com.google.firebase.Timestamp

data class Coupon(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val partnerName: String = "",
    val partnerLogoUrl: String = "",
    val category: String = "",
    val pointsRequired: Int = 0,
    val termsAndConditions: String = "",
    val howToRedeem: String = "",
    val featured: Boolean = false,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null
)
