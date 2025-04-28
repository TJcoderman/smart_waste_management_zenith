package com.hackathon.smartdustbin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.adapters.CouponAdapter
import com.hackathon.smartdustbin.adapters.RewardsAdapter
import com.hackathon.smartdustbin.models.Coupon
import com.hackathon.smartdustbin.utils.AnimationUtils

class RewardsFragment : Fragment() {

    private lateinit var totalPointsText: TextView
    private lateinit var availablePointsText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var featuredRecyclerView: RecyclerView
    private lateinit var coinAnimation: LottieAnimationView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var couponAdapter: CouponAdapter
    private lateinit var featuredAdapter: RewardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rewards, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews(view)
        
        // Setup animations
        setupAnimations()
        
        // Load user points
        loadUserPoints()
        
        // Setup tabs and viewpager
        setupTabsAndViewPager()
        
        // Setup featured rewards
        setupFeaturedRewards()
        
        return view
    }
    
    private fun initViews(view: View) {
        totalPointsText = view.findViewById(R.id.totalPointsText)
        availablePointsText = view.findViewById(R.id.availablePointsText)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        featuredRecyclerView = view.findViewById(R.id.featuredRecyclerView)
        coinAnimation = view.findViewById(R.id.coinAnimation)
    }
    
    private fun setupAnimations() {
        // Play coin animation
        coinAnimation.playAnimation()
        
        // Apply entry animations to views
        AnimationUtils.applyEntryAnimation(totalPointsText, 100)
        AnimationUtils.applyEntryAnimation(tabLayout, 200)
    }
    
    private fun loadUserPoints() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val totalPoints = document.getLong("total_points") ?: 0
                        val usedPoints = document.getLong("used_points") ?: 0
                        val availablePoints = totalPoints - usedPoints
                        
                        // Update UI with animations
                        updatePointsUI(totalPoints, availablePoints)
                        
                        // Clear badge count on rewards tab since user viewed it
                        clearRewardsBadge(userId)
                    }
                }
        }
    }
    
    private fun updatePointsUI(totalPoints: Long, availablePoints: Long) {
        // Animate points counter
        animatePointsCounter(totalPointsText, 0, totalPoints.toInt())
        animatePointsCounter(availablePointsText, 0, availablePoints.toInt())
    }
    
    private fun animatePointsCounter(textView: TextView, from: Int, to: Int) {
        val animator = ValueAnimator.ofInt(from, to)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            textView.text = value.toString()
        }
        animator.start()
    }
    
    private fun clearRewardsBadge(userId: String) {
        // Update unread rewards count in Firestore
        db.collection("users").document(userId)
            .update("unreadRewards", 0)
    }
    
    private fun setupTabsAndViewPager() {
        // Create adapter for the viewpager
        val adapter = RewardsPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Connect TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Available"
                1 -> "Food & Drinks"
                2 -> "Shopping"
                else -> "Entertainment"
            }
        }.attach()
        
        // Add page change animation
        viewPager.setPageTransformer { page, position ->
            // Page transition animation
            val pageWidth = page.width
            when {
                position < -1 -> page.alpha = 0f
                position <= 1 -> {
                    page.alpha = Math.max(0.2f, 1 - Math.abs(position))
                    page.translationX = pageWidth * -position * 0.25f
                }
                else -> page.alpha = 0f
            }
        }
    }
    
    private fun setupFeaturedRewards() {
        // Initialize adapter
        featuredAdapter = RewardsAdapter(ArrayList(), true) { coupon ->
            // Handle featured reward click
            showRewardDetails(coupon)
        }
        
        // Setup RecyclerView
        featuredRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }
        
        // Load featured rewards
        loadFeaturedRewards()
    }
    
    private fun loadFeaturedRewards() {
        db.collection("partner_offers")
            .whereEqualTo("featured", true)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val couponsList = ArrayList<Coupon>()
                for (document in documents) {
                    val coupon = document.toObject(Coupon::class.java)
                    couponsList.add(coupon)
                }
                
                // Update RecyclerView with animation
                featuredAdapter.updateData(couponsList)
            }
    }
    
    private fun showRewardDetails(coupon: Coupon) {
        // Create and show reward details dialog
        val dialog = RewardDetailsDialog.newInstance(coupon)
        dialog.show(childFragmentManager, "RewardDetailsDialog")
    }
    
    /**
     * ViewPager adapter for reward categories
     */
    inner class RewardsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        
        override fun getItemCount(): Int = 4
        
        override fun createFragment(position: Int): Fragment {
            return RewardCategoryFragment.newInstance(
                when (position) {
                    0 -> "all"
                    1 -> "food_drinks"
                    2 -> "shopping"
                    else -> "entertainment"
                }
            )
        }
    }
    
    /**
     * Fragment for each reward category tab
     */
    class RewardCategoryFragment : Fragment() {
        
        private lateinit var recyclerView: RecyclerView
        private lateinit var emptyView: View
        private lateinit var loadingAnimation: LottieAnimationView
        private lateinit var couponAdapter: CouponAdapter
        private lateinit var db: FirebaseFirestore
        private var category: String = "all"
        
        companion object {
            fun newInstance(category: String): RewardCategoryFragment {
                val fragment = RewardCategoryFragment()
                val args = Bundle()
                args.putString("category", category)
                fragment.arguments = args
                return fragment
            }
        }
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                category = it.getString("category") ?: "all"
            }
        }
        
        override fun onCreateView(
            inflater: LayoutInflater, 
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_reward_category, container, false)
            
            // Initialize Firestore
            db = FirebaseFirestore.getInstance()
            
            // Initialize views
            recyclerView = view.findViewById(R.id.rewardsRecyclerView)
            emptyView = view.findViewById(R.id.emptyView)
            loadingAnimation = view.findViewById(R.id.loadingAnimation)
            
            // Setup RecyclerView
            couponAdapter = CouponAdapter(ArrayList()) { coupon ->
                // Handle coupon click
                (parentFragment as? RewardsFragment)?.showRewardDetails(coupon)
            }
            
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = couponAdapter
            }
            
            // Show loading
            showLoading()
            
            // Load rewards for this category
            loadRewards()
            
            return view
        }
        
        private fun showLoading() {
            loadingAnimation.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
        }
        
        private fun showEmpty() {
            loadingAnimation.visibility = View.GONE
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }
        
        private fun showContent() {
            loadingAnimation.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
        
        private fun loadRewards() {
            var query = db.collection("partner_offers")
            
            // Apply category filter if not "all"
            if (category != "all") {
                query = query.whereEqualTo("category", category)
            }
            
            query.get().addOnSuccessListener { documents ->
                val couponsList = ArrayList<Coupon>()
                for (document in documents) {
                    val coupon = document.toObject(Coupon::class.java)
                    couponsList.add(coupon)
                }
                
                if (couponsList.isEmpty()) {
                    showEmpty()
                } else {
                    couponAdapter.updateData(couponsList)
                    showContent()
                }
            }
        }
    }
}