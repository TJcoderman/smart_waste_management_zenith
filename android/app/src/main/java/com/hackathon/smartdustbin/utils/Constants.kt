package com.hackathon.smartdustbin.utils

/**
 * Constants used throughout the app
 */
object Constants {

    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_WASTE_DEPOSITS = "waste_deposits"
    const val COLLECTION_SMART_BINS = "smart_bins"
    const val COLLECTION_PARTNER_OFFERS = "partner_offers"
    const val COLLECTION_REWARDS = "rewards"

    // Waste Types
    const val WASTE_TYPE_ORGANIC = "organic"
    const val WASTE_TYPE_PLASTIC = "recyclable_plastic"
    const val WASTE_TYPE_PAPER = "recyclable_paper"
    const val WASTE_TYPE_METAL = "recyclable_metal"

    // Points Calculation
    const val POINTS_PER_KG_ORGANIC = 5
    const val POINTS_PER_KG_PLASTIC = 10
    const val POINTS_PER_KG_PAPER = 8
    const val POINTS_PER_KG_METAL = 15

    // User Ranks
    const val RANK_NOVICE = "Novice Recycler"
    const val RANK_ECO_ROOKIE = "Eco Rookie"
    const val RANK_GREEN_GUARDIAN = "Green Guardian"
    const val RANK_ECO_WARRIOR = "Eco Warrior"
    const val RANK_MASTER_RECYCLER = "Master Recycler"

    // Achievement Thresholds
    val ACHIEVEMENT_THRESHOLDS = listOf(10, 25, 50, 100, 200, 500)

    // Partner Categories
    const val CATEGORY_FOOD_DRINKS = "food_drinks"
    const val CATEGORY_SHOPPING = "shopping"
    const val CATEGORY_ENTERTAINMENT = "entertainment"

    // Sample QR Code Format
    const val SAMPLE_QR_FORMAT = """
        {
            "bin_id": "bin123",
            "waste_type": "recyclable_plastic",
            "weight": 1.5
        }
    """

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 300L
    const val ANIMATION_DURATION_MEDIUM = 500L
    const val ANIMATION_DURATION_LONG = 1000L
    
    // Environmental Impact Calculations
    const val CO2_SAVED_PER_KG_PLASTIC = 2.5 // kg CO2 saved per kg of plastic recycled
    const val CO2_SAVED_PER_KG_PAPER = 1.8 // kg CO2 saved per kg of paper recycled
    const val CO2_SAVED_PER_KG_METAL = 4.5 // kg CO2 saved per kg of metal recycled
    
    // Trees Saved Calculation (approximate)
    const val PAPER_KG_PER_TREE = 80.0 // One tree produces roughly 80kg of paper
    
    // Level Calculation
    const val POINTS_PER_LEVEL = 100 // Points needed to level up
    
    // Maps Configuration
    const val DEFAULT_MAP_ZOOM = 15f
    
    // API URLs (for server connection)
    const val BASE_API_URL = "https://smartdustbin-api.example.com"
    const val DEPOSIT_ENDPOINT = "/api/simulate/deposit"
    const val BINS_ENDPOINT = "/api/bins"
    const val EMPTY_BIN_ENDPOINT = "/api/simulate/empty-bin"
}