package com.hackathon.smartdustbin.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.budiyev.android.codescanner.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import org.json.JSONObject
import java.util.*

class QrScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var backButton: ImageView
    private lateinit var scanAnimation: LottieAnimationView
    private lateinit var scanText: TextView
    private lateinit var resultCard: CardView
    private lateinit var successAnimation: LottieAnimationView
    private lateinit var resultTitle: TextView
    private lateinit var resultText: TextView
    private lateinit var pointsEarned: TextView
    private lateinit var closeButton: Button

    private lateinit var slideUpAnimation: Animation
    private lateinit var slideDownAnimation: Animation

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val CAMERA_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews()
        setupAnimations()

        if (hasCameraPermission()) {
            setupScanner()
        } else {
            requestCameraPermission()
        }

        setupClickListeners()
    }

    private fun initViews() {
        scannerView = findViewById(R.id.scannerView)
        backButton = findViewById(R.id.backButton)
        scanAnimation = findViewById(R.id.scanAnimation)
        scanText = findViewById(R.id.scanText)
        resultCard = findViewById(R.id.resultCard)
        successAnimation = findViewById(R.id.successAnimation)
        resultTitle = findViewById(R.id.resultTitle)
        resultText = findViewById(R.id.resultText)
        pointsEarned = findViewById(R.id.pointsEarned)
        closeButton = findViewById(R.id.closeButton)
    }

    private fun setupAnimations() {
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        scanAnimation.playAnimation()
        resultCard.visibility = View.GONE
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { onBackPressed() }

        closeButton.setOnClickListener {
            hideResultCard()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    private fun setupScanner() {
        codeScanner = CodeScanner(this, scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback { result ->
                runOnUiThread { processQrCode(result.text) }
            }

            errorCallback = { error ->
                runOnUiThread {
                    Toast.makeText(this@QrScanActivity, "Scanner error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun processQrCode(qrContent: String) {
        try {
            val jsonObject = JSONObject(qrContent)
            val binId = jsonObject.optString("bin_id")
            val wasteType = jsonObject.optString("waste_type")
            val weight = jsonObject.optDouble("weight", 0.0)

            if (binId.isNotBlank() && wasteType.isNotBlank() && weight > 0) {
                recordWasteDeposit(binId, wasteType, weight)
            } else {
                showResult(false, "Invalid QR code")
            }
        } catch (e: Exception) {
            showResult(false, "Invalid QR format")
        }
    }

    private fun recordWasteDeposit(binId: String, wasteType: String, weight: Double) {
        val userId = auth.currentUser?.uid ?: return showResult(false, "User not logged in")

        val pointsPerKg = when (wasteType) {
            "organic" -> 5
            "recyclable_plastic" -> 10
            "recyclable_paper" -> 8
            "recyclable_metal" -> 15
            else -> 1
        }
        val points = (weight * pointsPerKg).toInt()

        val deposit = hashMapOf(
            "user_id" to userId,
            "bin_id" to binId,
            "waste_type" to wasteType,
            "weight" to weight,
            "points_earned" to points,
            "timestamp" to Date()
        )

        db.collection("waste_deposits")
            .add(deposit)
            .addOnSuccessListener {
                updateUserPoints(userId, points)
                showResult(true, "You deposited ${String.format("%.1f", weight)} kg of ${wasteType.replace("_", " ").replaceFirstChar { it.uppercase() }} waste", points)
            }
            .addOnFailureListener { e ->
                showResult(false, "Error recording deposit: ${e.message}")
            }
    }

    private fun updateUserPoints(userId: String, earnedPoints: Int) {
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("total_points") ?: 0
            val newPoints = currentPoints + earnedPoints

            transaction.update(userRef, "total_points", newPoints)

            val currentLevel = (currentPoints / 100) + 1
            val newLevel = (newPoints / 100) + 1

            if (newLevel > currentLevel) {
                val newRank = when {
                    newLevel >= 10 -> "Master Recycler"
                    newLevel >= 5 -> "Eco Warrior"
                    newLevel >= 3 -> "Green Guardian"
                    else -> "Eco Rookie"
                }
                transaction.update(userRef, "rank", newRank)
            }
            null
        }
    }

    private fun showResult(success: Boolean, message: String, points: Int = 0) {
        resultTitle.text = if (success) "Thank You!" else "Oops!"
        resultText.text = message
        pointsEarned.visibility = if (success) View.VISIBLE else View.GONE
        pointsEarned.text = if (success) "+$points points" else ""

        successAnimation.setAnimation(
            if (success) R.raw.success_animation else R.raw.error_animation
        )
        successAnimation.playAnimation()

        resultCard.visibility = View.VISIBLE
        resultCard.startAnimation(slideUpAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            if (resultCard.visibility == View.VISIBLE) {
                closeButton.performClick()
            }
        }, if (success) 5000 else 3000)
    }

    private fun hideResultCard() {
        resultCard.startAnimation(slideDownAnimation)
        slideDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                resultCard.visibility = View.GONE
                codeScanner.startPreview()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }
}
