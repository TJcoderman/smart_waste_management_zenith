package com.hackathon.smartdustbin.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.utils.AnimationUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView
    private lateinit var recycleAnimation: LottieAnimationView
    private lateinit var loadingAnimation: LottieAnimationView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews()
        
        // Apply animations
        setupAnimations()
        
        // Setup button click listeners
        setupClickListeners()
    }
    
    private fun initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginText = findViewById(R.id.loginText)
        recycleAnimation = findViewById(R.id.recycleAnimation)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        
        // Initially hide loading animation
        loadingAnimation.visibility = View.GONE
    }
    
    private fun setupAnimations() {
        // Play recycle animation
        recycleAnimation.playAnimation()
        
        // Apply entry animations
        val registerViews = listOf(
            findViewById(R.id.titleText),
            findViewById(R.id.subtitleText),
            nameInputLayout,
            emailInputLayout,
            passwordInputLayout,
            confirmPasswordInputLayout,
            registerButton,
            findViewById(R.id.bottomTextLayout)
        )
        
        AnimationUtils.sequentialFadeIn(registerViews, 100)
    }
    
    private fun setupClickListeners() {
        // Register button click
        registerButton.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            registerUser()
        }
        
        // Login text click
        loginText.setOnClickListener {
            // Navigate back to login activity
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
    
    private fun registerUser() {
        // Get input values
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        
        // Validate input
        if (name.isEmpty()) {
            nameInputLayout.error = "Name is required"
            return
        } else {
            nameInputLayout.error = null
        }
        
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        } else {
            emailInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            return
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            return
        } else {
            passwordInputLayout.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            return
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords don't match"
            return
        } else {
            confirmPasswordInputLayout.error = null
        }
        
        // Show loading animation
        showLoading(true)
        
        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update user profile and create Firestore document
                    val user = auth.currentUser
                    
                    // Update profile with display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Create user document in Firestore
                                createUserDocument(user.uid, name, email)
                            } else {
                                showLoading(false)
                                Toast.makeText(this, "Error updating profile: ${profileTask.exception?.message}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // If sign up fails, display a message to the user
                    showLoading(false)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                    
                    // Shake fields to indicate error
                    AnimationUtils.applyShakeAnimation(emailInputLayout)
                }
            }
    }
    
    private fun createUserDocument(userId: String, name: String, email: String) {
        // Create user data map
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "total_points" to 0,
            "used_points" to 0,
            "rank" to "Novice Recycler",
            "unreadRewards" to 0,
            "created_at" to com.google.firebase.Timestamp.now()
        )
        
        // Add to Firestore
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                
                // Navigate to home activity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error creating user profile: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingAnimation.visibility = View.VISIBLE
            loadingAnimation.playAnimation()
            registerButton.visibility = View.INVISIBLE
        } else {
            loadingAnimation.visibility = View.GONE
            loadingAnimation.cancelAnimation()
            registerButton.visibility = View.VISIBLE
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}