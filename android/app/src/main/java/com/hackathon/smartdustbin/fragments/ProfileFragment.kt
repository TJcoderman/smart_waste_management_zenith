package com.hackathon.smartdustbin.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.activities.LoginActivity
import com.hackathon.smartdustbin.models.User
import com.hackathon.smartdustbin.utils.AnimationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var joinDateText: TextView
    private lateinit var levelText: TextView
    private lateinit var pointsText: TextView
    private lateinit var rankText: TextView
    private lateinit var depositsText: TextView
    private lateinit var editProfileButton: Button
    private lateinit var notificationsCard: CardView
    private lateinit var privacyCard: CardView
    private lateinit var helpCard: CardView
    private lateinit var aboutCard: CardView
    private lateinit var logoutButton: Button
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews(view)
        
        // Apply animations
        setupAnimations()
        
        // Load user profile
        loadUserProfile()
        
        // Setup click listeners
        setupClickListeners()
        
        return view
    }
    
    private fun initViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        nameText = view.findViewById(R.id.nameText)
        emailText = view.findViewById(R.id.emailText)
        joinDateText = view.findViewById(R.id.joinDateText)
        levelText = view.findViewById(R.id.levelText)
        pointsText = view.findViewById(R.id.pointsText)
        rankText = view.findViewById(R.id.rankText)
        depositsText = view.findViewById(R.id.depositsText)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        notificationsCard = view.findViewById(R.id.notificationsCard)
        privacyCard = view.findViewById(R.id.privacyCard)
        helpCard = view.findViewById(R.id.helpCard)
        aboutCard = view.findViewById(R.id.aboutCard)
        logoutButton = view.findViewById(R.id.logoutButton)
    }
    
    private fun setupAnimations() {
        // Apply entry animations with staggered delay
        val profileHeaderElements = listOf(
            profileImage, nameText, emailText, joinDateText, 
            editProfileButton
        )
        
        AnimationUtils.sequentialFadeIn(profileHeaderElements, 100)
        
        // Stats card animation
        AnimationUtils.applyEntryAnimation(view?.findViewById(R.id.statsCard), 300)
        
        // Settings card animations
        val settingsCards = listOf(
            notificationsCard, privacyCard, helpCard, aboutCard
        )
        AnimationUtils.sequentialFadeIn(settingsCards, 150, 500)
        
        // Logout button animation
        AnimationUtils.applyEntryAnimation(logoutButton, 800)
        
        // Apply click animations to cards
        arrayOf(notificationsCard, privacyCard, helpCard, aboutCard).forEach { card ->
            card.setOnClickListener {
                AnimationUtils.applyClickAnimation(it)
            }
        }
    }
    
    private fun loadUserProfile() {
        val firebaseUser = auth.currentUser
        
        if (firebaseUser != null) {
            // Update UI with basic info
            updateBasicInfo(firebaseUser)
            
            // Get additional user data from Firestore
            db.collection("users").document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentUser = document.toObject(User::class.java)
                        updateUserStats()
                    }
                }
            
            // Get deposits count
            db.collection("waste_deposits")
                .whereEqualTo("user_id", firebaseUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val depositsCount = documents.size()
                    AnimationUtils.animateCounter(depositsText, 0, depositsCount)
                }
        }
    }
    
    private fun updateBasicInfo(user: FirebaseUser) {
        // Set name and email
        nameText.text = user.displayName ?: "Eco Warrior"
        emailText.text = user.email
        
        // Set join date
        val creationTimestamp = user.metadata?.creationTimestamp
        if (creationTimestamp != null) {
            val date = Date(creationTimestamp)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            joinDateText.text = "Joined ${sdf.format(date)}"
        } else {
            joinDateText.text = "Recently joined"
        }
        
        // Load profile image
        val photoUrl = user.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImage)
        }
    }
    
    private fun updateUserStats() {
        currentUser?.let { user ->
            // Set points
            AnimationUtils.animateCounter(pointsText, 0, user.total_points.toInt())
            
            // Set level and rank
            val level = (user.total_points / 100) + 1
            levelText.text = "Level $level"
            rankText.text = user.rank
        }
    }
    
    private fun setupClickListeners() {
        // Edit profile button
        editProfileButton.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            // TODO: Open edit profile activity
        }
        
        // Notification settings
        notificationsCard.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            // TODO: Open notification settings
        }
        
        // Privacy settings
        privacyCard.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            // TODO: Open privacy settings
        }
        
        // Help & Support
        helpCard.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            // TODO: Open help activity
        }
        
        // About
        aboutCard.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            // TODO: Open about activity
        }
        
        // Logout button
        logoutButton.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            showLogoutConfirmation()
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Logout user
                auth.signOut()
                
                // Navigate to login screen
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}