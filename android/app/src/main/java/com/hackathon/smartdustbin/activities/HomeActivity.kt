package com.hackathon.smartdustbin.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.fragments.HomeFragment
import com.hackathon.smartdustbin.fragments.ImpactFragment
import com.hackathon.smartdustbin.fragments.ProfileFragment
import com.hackathon.smartdustbin.fragments.RewardsFragment
import com.hackathon.smartdustbin.models.User
import com.hackathon.smartdustbin.utils.AnimationUtils as AnimUtils

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabScan: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        bottomNav = findViewById(R.id.bottomNav)
        fabScan = findViewById(R.id.fabScan)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up FAB scan button with bounce animation
        setupFabScan()

        // Set up navigation drawer
        setupNavDrawer()

        // Load user data
        loadUserData()

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            var fragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> fragment = HomeFragment()
                R.id.nav_rewards -> fragment = RewardsFragment()
                R.id.nav_impact -> fragment = ImpactFragment()
                R.id.nav_profile -> fragment = ProfileFragment()
            }
            
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }
    }

    private fun setupFabScan() {
        // Apply pulsing animation to FAB
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        fabScan.startAnimation(pulseAnimation)

        fabScan.setOnClickListener {
            // Apply click animation
            AnimUtils.applyClickAnimation(it)
            
            // Start QR code scanning activity
            val intent = Intent(this, QrScanActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    private fun setupNavDrawer() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    // Open settings
                    true
                }
                R.id.nav_help -> {
                    // Open help
                    true
                }
                R.id.nav_logout -> {
                    // Logout user
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentUser = document.toObject(User::class.java)
                        // Update UI with user data
                        updateUserUI()
                    }
                }
        }
    }

    private fun updateUserUI() {
        val headerView = navView.getHeaderView(0)
        // Update navigation drawer header with user info
        headerView.findViewById<android.widget.TextView>(R.id.userNameText).text = currentUser?.name
        headerView.findViewById<android.widget.TextView>(R.id.userEmailText).text = currentUser?.email
        
        // Update badge count for rewards if available
        if (currentUser?.unreadRewards ?: 0 > 0) {
            bottomNav.getOrCreateBadge(R.id.nav_rewards).apply {
                number = currentUser?.unreadRewards ?: 0
                isVisible = true
            }
        }
    }

    // Open drawer when menu button clicked
    fun openDrawer(view: View) {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}