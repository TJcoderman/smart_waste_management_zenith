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
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.utils.AnimationUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var recycleAnimation: LottieAnimationView
    private lateinit var loadingAnimation: LottieAnimationView
    
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initViews()
        
        // Apply animations
        setupAnimations()
        
        // Setup button click listeners
        setupClickListeners()
    }
    
    override fun onStart() {
        super.onStart()
        // Check if user is signed in and update UI accordingly
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, go to home activity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
    
    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerText = findViewById(R.id.registerText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        recycleAnimation = findViewById(R.id.recycleAnimation)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        
        // Initially hide loading animation
        loadingAnimation.visibility = View.GONE
    }
    
    private fun setupAnimations() {
        // Play recycle animation
        recycleAnimation.playAnimation()
        
        // Apply entry animations
        val loginViews = listOf(
            findViewById(R.id.titleText),
            findViewById(R.id.subtitleText),
            emailInputLayout,
            passwordInputLayout,
            loginButton,
            findViewById(R.id.orText),
            findViewById(R.id.socialLoginLayout),
            findViewById(R.id.bottomTextLayout)
        )
        
        AnimationUtils.sequentialFadeIn(loginViews, 100)
    }
    
    private fun setupClickListeners() {
        // Login button click
        loginButton.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            loginUser()
        }
        
        // Register text click
        registerText.setOnClickListener {
            // Navigate to register activity
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        // Forgot password text click
        forgotPasswordText.setOnClickListener {
            // Show forgot password dialog
            showForgotPasswordDialog()
        }
        
        // Social login buttons (to be implemented in a real app)
        findViewById<View>(R.id.googleLoginButton).setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            Toast.makeText(this, "Google Login - To be implemented", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<View>(R.id.facebookLoginButton).setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            Toast.makeText(this, "Facebook Login - To be implemented", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loginUser() {
        // Get email and password
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        // Validate input
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        } else {
            emailInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            return
        } else {
            passwordInputLayout.error = null
        }
        
        // Show loading animation
        showLoading(true)
        
        // Sign in with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to home activity
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                    
                    // Shake password field to indicate error
                    AnimationUtils.applyShakeAnimation(passwordInputLayout)
                }
            }
    }
    
    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.forgotPasswordEmailInput)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Reset") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email", 
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingAnimation.visibility = View.VISIBLE
            loadingAnimation.playAnimation()
            loginButton.visibility = View.INVISIBLE
        } else {
            loadingAnimation.visibility = View.GONE
            loadingAnimation.cancelAnimation()
            loginButton.visibility = View.VISIBLE
        }
    }
}