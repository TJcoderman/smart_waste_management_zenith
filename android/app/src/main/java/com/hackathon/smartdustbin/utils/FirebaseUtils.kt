package com.hackathon.smartdustbin.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hackathon.smartdustbin.models.Bin
import com.hackathon.smartdustbin.models.Coupon
import com.hackathon.smartdustbin.models.Deposit
import com.hackathon.smartdustbin.models.Reward
import com.hackathon.smartdustbin.models.User
import java.util.Date

/**
 * Utility class for Firebase operations
 */
object FirebaseUtils {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    // User related functions
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get user data
     */
    fun getUserData(userId: String, callback: (User?) -> Unit) {
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
    
    /**
     * Update user points
     */
    fun updateUserPoints(userId: String, pointsToAdd: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userRef = db.collection(Constants.COLLECTION_USERS).document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("total_points") ?: 0
            val newPoints = currentPoints + pointsToAdd
            
            transaction.update(userRef, "total_points", newPoints)
            
            // Check if level changed and update rank
            val currentLevel = (currentPoints / Constants.POINTS_PER_LEVEL) + 1
            val newLevel = (newPoints / Constants.POINTS_PER_LEVEL) + 1
            
            if (newLevel > currentLevel) {
                val newRank = when {
                    newLevel >= 10 -> Constants.RANK_MASTER_RECYCLER
                    newLevel >= 5 -> Constants.RANK_ECO_WARRIOR
                    newLevel >= 3 -> Constants.RANK_GREEN_GUARDIAN
                    else -> Constants.RANK_ECO_ROOKIE
                }
                transaction.update(userRef, "rank", newRank)
            }
            
            null
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }
    
    /**
     * Check if user has enough points for a reward
     */
    fun checkUserPoints(userId: String, pointsRequired: Int, callback: (Boolean) -> Unit) {
        db.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalPoints = document.getLong("total_points") ?: 0
                    val usedPoints = document.getLong("used_points") ?: 0
                    val availablePoints = totalPoints - usedPoints
                    
                    callback(availablePoints >= pointsRequired)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
    
    // Waste Deposit related functions
    
    /**
     * Record a new waste deposit
     */
    fun recordWasteDeposit(
        userId: String,
        binId: String,
        wasteType: String,
        weight: Double,
        onSuccess: (Deposit) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Calculate points based on waste type and weight
        val pointsPerKg = when (wasteType) {
            Constants.WASTE_TYPE_ORGANIC -> Constants.POINTS_PER_KG_ORGANIC
            Constants.WASTE_TYPE_PLASTIC -> Constants.POINTS_PER_KG_PLASTIC
            Constants.WASTE_TYPE_PAPER -> Constants.POINTS_PER_KG_PAPER
            Constants.WASTE_TYPE_METAL -> Constants.POINTS_PER_KG_METAL
            else -> 1
        }
        
        val pointsEarned = (weight * pointsPerKg).toLong()
        
        // Create deposit object
        val timestamp = com.google.firebase.Timestamp.now()
        val deposit = Deposit(
            user_id = userId,
            bin_id = binId,
            waste_type = wasteType,
            weight = weight,
            points_earned = pointsEarned,
            timestamp = timestamp
        )
        
        // Save to Firestore
        db.collection(Constants.COLLECTION_WASTE_DEPOSITS)
            .add(deposit)
            .addOnSuccessListener { documentReference ->
                // Update user points
                updateUserPoints(
                    userId, 
                    pointsEarned.toInt(),
                    { 
                        // Update bin fill level
                        updateBinFillLevel(binId, weight)
                        // Return deposit with success
                        onSuccess(deposit)
                    },
                    { e -> onFailure(e) }
                )
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Get user's waste deposit history
     */
    fun getWasteHistory(
        userId: String,
        limit: Long = 10,
        onSuccess: (List<Deposit>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_WASTE_DEPOSITS)
            .whereEqualTo("user_id", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { documents ->
                val depositsList = mutableListOf<Deposit>()
                for (document in documents) {
                    val deposit = document.toObject(Deposit::class.java)
                    depositsList.add(deposit)
                }
                onSuccess(depositsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Get user's waste statistics
     */
    fun getWasteStatistics(
        userId: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_WASTE_DEPOSITS)
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                var organicTotal = 0.0
                var plasticTotal = 0.0
                var paperTotal = 0.0
                var metalTotal = 0.0
                var totalDeposits = 0
                var totalPoints = 0L
                
                for (document in documents) {
                    val deposit = document.toObject(Deposit::class.java)
                    totalDeposits++
                    totalPoints += deposit.points_earned
                    
                    when (deposit.waste_type) {
                        Constants.WASTE_TYPE_ORGANIC -> organicTotal += deposit.weight
                        Constants.WASTE_TYPE_PLASTIC -> plasticTotal += deposit.weight
                        Constants.WASTE_TYPE_PAPER -> paperTotal += deposit.weight
                        Constants.WASTE_TYPE_METAL -> metalTotal += deposit.weight
                    }
                }
                
                // Calculate environmental impact
                val recyclableTotal = plasticTotal + paperTotal + metalTotal
                val co2Saved = (
                    plasticTotal * Constants.CO2_SAVED_PER_KG_PLASTIC +
                    paperTotal * Constants.CO2_SAVED_PER_KG_PAPER +
                    metalTotal * Constants.CO2_SAVED_PER_KG_METAL
                )
                val treesSaved = paperTotal / Constants.PAPER_KG_PER_TREE
                
                val stats = mapOf(
                    "organicTotal" to organicTotal,
                    "plasticTotal" to plasticTotal,
                    "paperTotal" to paperTotal,
                    "metalTotal" to metalTotal,
                    "recyclableTotal" to recyclableTotal,
                    "totalDeposits" to totalDeposits,
                    "totalPoints" to totalPoints,
                    "co2Saved" to co2Saved,
                    "treesSaved" to treesSaved
                )
                
                onSuccess(stats)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    // Smart Bin related functions
    
    /**
     * Get all smart bins
     */
    fun getSmartBins(
        onSuccess: (List<Bin>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_SMART_BINS)
            .get()
            .addOnSuccessListener { documents ->
                val binsList = mutableListOf<Bin>()
                for (document in documents) {
                    val bin = document.toObject(Bin::class.java)
                    binsList.add(bin)
                }
                onSuccess(binsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Update bin fill level
     */
    private fun updateBinFillLevel(binId: String, additionalWeight: Double) {
        val binRef = db.collection(Constants.COLLECTION_SMART_BINS).document(binId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(binRef)
            if (snapshot.exists()) {
                val currentFillLevel = snapshot.getDouble("current_fill_level") ?: 0.0
                // Assuming 1kg = 2% fill level (adjust as needed)
                val newFillLevel = minOf(100.0, currentFillLevel + (additionalWeight * 2))
                
                transaction.update(binRef, "current_fill_level", newFillLevel)
                
                // If bin is full, update status
                if (newFillLevel >= 90) {
                    transaction.update(binRef, "status", "full")
                }
            }
            null
        }
    }
    
    /**
     * Empty a smart bin (reset fill level)
     */
    fun emptySmartBin(
        binId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val binRef = db.collection(Constants.COLLECTION_SMART_BINS).document(binId)
        
        val updates = hashMapOf<String, Any>(
            "current_fill_level" to 0.0,
            "status" to "active",
            "last_emptied" to com.google.firebase.Timestamp.now()
        )
        
        binRef.update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    // Reward related functions
    
    /**
     * Get available rewards
     */
    fun getAvailableRewards(
        category: String? = null,
        onSuccess: (List<Coupon>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query = db.collection(Constants.COLLECTION_PARTNER_OFFERS)
        
        // Apply category filter if provided
        if (category != null && category != "all") {
            query = query.whereEqualTo("category", category)
        }
        
        query.get()
            .addOnSuccessListener { documents ->
                val couponsList = mutableListOf<Coupon>()
                for (document in documents) {
                    val coupon = document.toObject(Coupon::class.java)
                    couponsList.add(coupon)
                }
                onSuccess(couponsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Get featured rewards
     */
    fun getFeaturedRewards(
        limit: Long = 5,
        onSuccess: (List<Coupon>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_PARTNER_OFFERS)
            .whereEqualTo("featured", true)
            .limit(limit)
            .get()
            .addOnSuccessListener { documents ->
                val couponsList = mutableListOf<Coupon>()
                for (document in documents) {
                    val coupon = document.toObject(Coupon::class.java)
                    couponsList.add(coupon)
                }
                onSuccess(couponsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    
    /**
     * Redeem a reward
     */
    fun redeemReward(
        userId: String,
        coupon: Coupon,
        couponCode: String,
        onSuccess: (Reward) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Calculate expiry date (30 days from now)
        val currentTime = com.google.firebase.Timestamp.now()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = Date(currentTime.seconds * 1000)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
        val expiryDate = com.google.firebase.Timestamp(calendar.time.time / 1000, 0)
        
        // Create reward object
        val reward = Reward(
            user_id = userId,
            coupon_id = coupon.id,
            partner_id = coupon.id.split("_").firstOrNull() ?: "",
            coupon_code = couponCode,
            points_redeemed = coupon.pointsRequired,
            created_at = currentTime,
            expiry_date = expiryDate,
            is_redeemed = false
        )
        
        // First check if user has enough points
        checkUserPoints(userId, coupon.pointsRequired) { hasEnoughPoints ->
            if (hasEnoughPoints) {
                // Add to Firestore
                db.collection(Constants.COLLECTION_REWARDS)
                    .add(reward)
                    .addOnSuccessListener { documentReference ->
                        // Update user's used points
                        updateUserUsedPoints(userId, coupon.pointsRequired)
                        onSuccess(reward)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            } else {
                onFailure(Exception("Not enough points"))
            }
        }
    }
    
    /**
     * Update user's used points
     */
    private fun updateUserUsedPoints(userId: String, pointsUsed: Int) {
        val userRef = db.collection(Constants.COLLECTION_USERS).document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentUsedPoints = snapshot.getLong("used_points") ?: 0
            val newUsedPoints = currentUsedPoints + pointsUsed
            
            transaction.update(userRef, "used_points", newUsedPoints)
            null
        }
    }
    
    /**
     * Get user's redeemed rewards
     */
    fun getUserRewards(
        userId: String,
        onSuccess: (List<Reward>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_REWARDS)
            .whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val rewardsList = mutableListOf<Reward>()
                for (document in documents) {
                    val reward = document.toObject(Reward::class.java)
                    rewardsList.add(reward)
                }
                onSuccess(rewardsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}