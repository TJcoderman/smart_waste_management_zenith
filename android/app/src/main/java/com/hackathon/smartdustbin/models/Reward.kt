package com.hackathon.smartdustbin.models

import com.google.firebase.Timestamp

data class Reward(
    val id: String = "",
    val user_id: String = "",
    val coupon_id: String = "",
    val partner_id: String = "",
    val coupon_code: String = "",
    val points_redeemed: Int = 0,
    val created_at: Timestamp? = null,
    val expiry_date: Timestamp? = null,
    val is_redeemed: Boolean = false
)
