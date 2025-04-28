package com.hackathon.smartdustbin.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.adapters.HistoryAdapter
import com.hackathon.smartdustbin.models.Deposit
import com.hackathon.smartdustbin.utils.AnimationUtils

class HomeFragment : Fragment() {

    private lateinit var profileCard: CardView
    private lateinit var achievementCard: CardView
    private lateinit var statsCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var pointsText: TextView
    private lateinit var pointsProgressBar: ProgressBar
    private lateinit var levelText: TextView
    private lateinit var rankText: TextView
    private lateinit var recycleCount: TextView
    private lateinit var achievementAnimation: LottieAnimationView
    private lateinit var achievementProgress: ProgressBar
    private lateinit var achievementText: TextView
    private lateinit var organicCount: TextView
    private lateinit var recyclableCount: TextView
    private lateinit var co2Saved: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var totalPointsEarned: TextView
    private lateinit var confettiAnimation: LottieAnimationView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews(view)
        
        // Setup animations and UI
        setupAnimations()
        
        // Load user data
        loadUserData()
        
        // Load stats
        loadStatistics()
        
        // Load history
        setupHistoryRecyclerView()
        loadHistory()

        return view
    }
    
    private fun initViews(view: View) {
        profileCard = view.findViewById(R.id.profileCard)
        achievementCard = view.findViewById(R.id.achievementCard)
        statsCard = view.findViewById(R.id.statsCard)
        historyCard = view.findViewById(R.id.historyCard)
        pointsText = view.findViewById(R.id.pointsText)
        pointsProgressBar = view.findViewById(R.id.pointsProgressBar)
        levelText = view.findViewById(R.id.levelText)
        rankText = view.findViewById(R.id.rankText)
        recycleCount = view.findViewById(R.id.recycleCount)
        achievementAnimation = view.findViewById(R.id.achievementAnimation)
        achievementProgress = view.findViewById(R.id.achievementProgress)
        achievementText = view.findViewById(R.id.achievementText)
        organicCount = view.findViewById(R.id.organicCount)
        recyclableCount = view.findViewById(R.id.recyclableCount)
        co2Saved = view.findViewById(R.id.co2Saved)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        totalPointsEarned = view.findViewById(R.id.totalPointsEarned)
        confettiAnimation = view.findViewById(R.id.confettiAnimation)
    }
    
    private fun setupAnimations() {
        // Animate cards entry
        AnimationUtils.applyEntryAnimation(profileCard, 100)
        AnimationUtils.applyEntryAnimation(achievementCard, 200)
        AnimationUtils.applyEntryAnimation(statsCard, 300)
        AnimationUtils.applyEntryAnimation(historyCard, 400)
        
        // Setup click animations
        arrayOf(profileCard, achievementCard, statsCard, historyCard).forEach { card ->
            card.setOnClickListener {
                AnimationUtils.applyClickAnimation(it)
            }
        }
        
        // Play achievement animation
        achievementAnimation.playAnimation()
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val points = document.getLong("total_points") ?: 0
                        val level = calculateLevel(points)
                        val rank = document.getString("rank") ?: "Novice Recycler"
                        
                        // Update UI with animations
                        updatePointsUI(points, level, rank)
                    }
                }
        }
    }
    
    private fun updatePointsUI(points: Long, level: Int, rank: String) {
        // Animate points text
        val currentPoints = pointsText.text.toString().toIntOrNull() ?: 0
        val animator = ValueAnimator.ofInt(currentPoints, points.toInt())
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            pointsText.text = animation.animatedValue.toString()
        }
        animator.start()
        
        // Animate progress bar
        val progressTo = (points % 100).toInt()
        ObjectAnimator.ofInt(pointsProgressBar, "progress", 0, progressTo).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
        }
        
        // Set level and rank
        levelText.text = "Level $level"
        rankText.text = rank
        
        // If first time loading and high level, show confetti
        if (level > 2 && !confettiAnimation.isAnimating) {
            confettiAnimation.visibility = View.VISIBLE
            confettiAnimation.playAnimation()
            
            // Hide confetti after animation completes
            confettiAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    confettiAnimation.visibility = View.GONE
                }
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
            })
        }
    }
    
    private fun calculateLevel(points: Long): Int {
        return (points / 100).toInt() + 1
    }
    
    private fun loadStatistics() {
        val userId = auth.currentUser?.uid ?: return
        
        // Get stats from Firestore
        db.collection("waste_deposits")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                var organicTotal = 0.0
                var recyclableTotal = 0.0
                var totalDeposits = 0
                var totalPoints = 0L
                
                for (document in documents) {
                    val deposit = document.toObject(Deposit::class.java)
                    totalDeposits++
                    totalPoints += deposit.points_earned ?: 0
                    
                    when (deposit.waste_type) {
                        "organic" -> organicTotal += deposit.weight ?: 0.0
                        "recyclable_plastic", "recyclable_paper", "recyclable_metal" -> 
                            recyclableTotal += deposit.weight ?: 0.0
                    }
                }
                
                // Update UI with animated counters
                recycleCount.text = totalDeposits.toString()
                
                // Animate counters
                animateCounter(organicCount, 0f, organicTotal.toFloat())
                animateCounter(recyclableCount, 0f, recyclableTotal.toFloat())
                
                // Calculate CO2 saved (approx 2.5 kg CO2 per kg of recycled waste)
                val co2SavedValue = recyclableTotal * 2.5
                animateCounter(co2Saved, 0f, co2SavedValue.toFloat())
                
                // Total points
                totalPointsEarned.text = totalPoints.toString()
                
                // Update achievement progress
                updateAchievementProgress(totalDeposits)
            }
    }
    
    private fun animateCounter(textView: TextView, from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            textView.text = String.format("%.1f kg", value)
        }
        animator.start()
    }
    
    private fun updateAchievementProgress(deposits: Int) {
        // Next achievement at 10, 25, 50, 100 deposits
        val achievements = listOf(10, 25, 50, 100, 200, 500)
        
        // Find next achievement target
        var nextTarget = 10
        var progressPercent = 0
        
        for (target in achievements) {
            if (deposits < target) {
                nextTarget = target
                progressPercent = (deposits * 100) / target
                break
            }
        }
        
        // If passed all achievements
        if (deposits >= achievements.last()) {
            nextTarget = achievements.last()
            progressPercent = 100
        }
        
        // Update UI
        achievementText.text = "Next achievement: $deposits/$nextTarget deposits"
        
        // Animate progress
        ObjectAnimator.ofInt(achievementProgress, "progress", 0, progressPercent).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            start()
        }
    }
    
    private fun setupHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(ArrayList())
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }
    
    private fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("waste_deposits")
            .whereEqualTo("user_id", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val depositsList = ArrayList<Deposit>()
                for (document in documents) {
                    val deposit = document.toObject(Deposit::class.java)
                    depositsList.add(deposit)
                }
                
                // Update RecyclerView with animation
                historyAdapter.updateData(depositsList)
            }
    }
}