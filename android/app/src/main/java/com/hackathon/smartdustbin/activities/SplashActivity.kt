package com.hackathon.smartdustbin.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.hackathon.smartdustbin.R

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var recycleAnimation: LottieAnimationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        logoImage = findViewById(R.id.logoImage)
        titleText = findViewById(R.id.titleText)
        subtitleText = findViewById(R.id.subtitleText)
        recycleAnimation = findViewById(R.id.recycleAnimation)

        // Set initial alpha to 0
        logoImage.alpha = 0f
        titleText.alpha = 0f
        subtitleText.alpha = 0f
        recycleAnimation.alpha = 0f

        // Start animations
        startAnimations()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 3500)
    }

    private fun startAnimations() {
        // Logo animation
        val logoAnim = ObjectAnimator.ofFloat(logoImage, View.ALPHA, 0f, 1f)
        logoAnim.duration = 1000
        logoAnim.interpolator = AccelerateDecelerateInterpolator()

        // Title animation
        val titleAnim = ObjectAnimator.ofFloat(titleText, View.ALPHA, 0f, 1f)
        titleAnim.duration = 800
        titleAnim.startDelay = 500
        titleAnim.interpolator = AccelerateDecelerateInterpolator()

        // Subtitle animation
        val subtitleAnim = ObjectAnimator.ofFloat(subtitleText, View.ALPHA, 0f, 1f)
        subtitleAnim.duration = 800
        subtitleAnim.startDelay = 700
        subtitleAnim.interpolator = AccelerateDecelerateInterpolator()

        // Recycle animation
        val recycleAnim = ObjectAnimator.ofFloat(recycleAnimation, View.ALPHA, 0f, 1f)
        recycleAnim.duration = 1000
        recycleAnim.startDelay = 1000
        recycleAnim.interpolator = AccelerateDecelerateInterpolator()

        // Create animator set and start
        val animSet = AnimatorSet()
        animSet.playTogether(logoAnim, titleAnim, subtitleAnim, recycleAnim)
        animSet.start()

        // Start Lottie animation
        recycleAnimation.playAnimation()
    }

    private fun checkUserAndNavigate() {
        // Check if user is logged in
        if (auth.currentUser != null) {
            // User is logged in, go to home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User is not logged in, go to login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // Apply slide transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}