package com.hackathon.smartdustbin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.models.Coupon
import com.hackathon.smartdustbin.utils.AnimationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RewardDetailsDialog : BottomSheetDialogFragment() {

    private lateinit var coupon: Coupon
    private lateinit var closeButton: ImageView
    private lateinit var couponImage: ImageView
    private lateinit var couponTitle: TextView
    private lateinit var partnerName: TextView
    private lateinit var validityText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var pointsRequired: TextView
    private lateinit var redeemButton: Button
    private lateinit var termsButton: TextView
    private lateinit var termsLayout: ConstraintLayout
    private lateinit var termsText: TextView
    private lateinit var termsCloseButton: Button
    private lateinit var successLayout: ConstraintLayout
    private lateinit var redeemSuccessAnimation: LottieAnimationView
    private lateinit var couponCodeText: TextView
    private lateinit var copyCodeButton: Button
    private lateinit var doneButton: Button
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        fun newInstance(coupon: Coupon): RewardDetailsDialog {
            val fragment = RewardDetailsDialog()
            val args = Bundle()
            args.putString("coupon_id", coupon.id)
            args.putString("title", coupon.title)
            args.putString("description", coupon.description)
            args.putString("imageUrl", coupon.imageUrl)
            args.putString("partnerName", coupon.partnerName)
            args.putInt("pointsRequired", coupon.pointsRequired)
            args.putString("termsAndConditions", coupon.termsAndConditions)
            args.putString("howToRedeem", coupon.howToRedeem)
            args.putLong("endDate", coupon.endDate?.seconds ?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Extract coupon data from arguments
        arguments?.let {
            coupon = Coupon(
                id = it.getString("coupon_id") ?: "",
                title = it.getString("title") ?: "",
                description = it.getString("description") ?: "",
                imageUrl = it.getString("imageUrl") ?: "",
                partnerName = it.getString("partnerName") ?: "",
                pointsRequired = it.getInt("pointsRequired", 0),
                termsAndConditions = it.getString("termsAndConditions") ?: "",
                howToRedeem = it.getString("howToRedeem") ?: ""
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_reward_details, container, false)
        
        // Initialize views
        initViews(view)
        
        // Populate UI with coupon data
        populateUI()
        
        // Setup click listeners
        setupClickListeners()
        
        return view
    }
    
    override fun onStart() {
        super.onStart()
        
        // Set the dialog to expanded state
        val dialog = dialog as? BottomSheetDialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
    }
    
    private fun initViews(view: View) {
        closeButton = view.findViewById(R.id.closeButton)
        couponImage = view.findViewById(R.id.couponImage)
        couponTitle = view.findViewById(R.id.couponTitle)
        partnerName = view.findViewById(R.id.partnerName)
        validityText = view.findViewById(R.id.validityText)
        descriptionText = view.findViewById(R.id.descriptionText)
        pointsRequired = view.findViewById(R.id.pointsRequired)
        redeemButton = view.findViewById(R.id.redeemButton)
        termsButton = view.findViewById(R.id.termsButton)
        termsLayout = view.findViewById(R.id.termsLayout)
        termsText = view.findViewById(R.id.termsText)
        termsCloseButton = view.findViewById(R.id.termsCloseButton)
        successLayout = view.findViewById(R.id.successLayout)
        redeemSuccessAnimation = view.findViewById(R.id.redeemSuccessAnimation)
        couponCodeText = view.findViewById(R.id.couponCodeText)
        copyCodeButton = view.findViewById(R.id.copyCodeButton)
        doneButton = view.findViewById(R.id.doneButton)
        
        // Initially hide terms and success layouts
        termsLayout.visibility = View.GONE
        successLayout.visibility = View.GONE
    }
    
    private fun populateUI() {
        // Set coupon details
        couponTitle.text = coupon.title
        partnerName.text = coupon.partnerName
        descriptionText.text = coupon.description
        pointsRequired.text = "${coupon.pointsRequired} points"
        termsText.text = coupon.termsAndConditions
        
        // Format validity date
        val endDate = coupon.endDate?.toDate()
        if (endDate != null) {
            validityText.text = "Valid till ${formatDate(endDate)}"
        } else {
            validityText.text = "No expiry date"
        }
        
        // Load coupon image with Glide
        Glide.with(requireContext())
            .load(coupon.imageUrl)
            .placeholder(R.drawable.placeholder_coupon)
            .error(R.drawable.placeholder_coupon)
            .centerCrop()
            .into(couponImage)
        
        // Check if user has enough points
        checkUserPoints()
    }
    
    private fun checkUserPoints() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val totalPoints = document.getLong("total_points") ?: 0
                        val usedPoints = document.getLong("used_points") ?: 0
                        val availablePoints = totalPoints - usedPoints
                        
                        // Update redeem button state
                        if (availablePoints >= coupon.pointsRequired) {
                            redeemButton.isEnabled = true
                            redeemButton.text = "Redeem for ${coupon.pointsRequired} points"
                        } else {
                            redeemButton.isEnabled = false
                            redeemButton.text = "Not enough points"
                            redeemButton.setBackgroundResource(R.drawable.rounded_button_disabled)
                        }
                    }
                }
        }
    }
    
    private fun setupClickListeners() {
        // Close dialog
        closeButton.setOnClickListener {
            dismiss()
        }
        
        // Terms and conditions
        termsButton.setOnClickListener {
            termsLayout.visibility = View.VISIBLE
            AnimationUtils.applyEntryAnimation(termsLayout)
        }
        
        // Close terms
        termsCloseButton.setOnClickListener {
            termsLayout.visibility = View.GONE
        }
        
        // Redeem coupon
        redeemButton.setOnClickListener {
            if (redeemButton.isEnabled) {
                AnimationUtils.applyClickAnimation(it)
                redeemCoupon()
            } else {
                AnimationUtils.applyShakeAnimation(it)
                Toast.makeText(context, "Not enough points!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Copy code
        copyCodeButton.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            copyToClipboard(couponCodeText.text.toString())
        }
        
        // Done button
        doneButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun redeemCoupon() {
        val userId = auth.currentUser?.uid ?: return
        
        // Generate a random coupon code
        val couponCode = generateCouponCode()
        
        // Calculate expiry date (30 days from now)
        val currentTime = com.google.firebase.Timestamp.now()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = Date(currentTime.seconds * 1000)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
        val expiryDate = com.google.firebase.Timestamp(calendar.time.time / 1000, 0)
        
        // Create reward document
        val reward = hashMapOf(
            "user_id" to userId,
            "coupon_id" to coupon.id,
            "partner_id" to coupon.id.split("_").firstOrNull() ?: "",
            "coupon_code" to couponCode,
            "points_redeemed" to coupon.pointsRequired,
            "created_at" to currentTime,
            "expiry_date" to expiryDate,
            "is_redeemed" to false
        )
        
        // Add to Firestore
        db.collection("rewards")
            .add(reward)
            .addOnSuccessListener { documentReference ->
                // Update user's used points
                updateUserPoints(userId, coupon.pointsRequired)
                
                // Show success UI
                showRedeemSuccess(couponCode)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateUserPoints(userId: String, pointsRedeemed: Int) {
        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentUsedPoints = snapshot.getLong("used_points") ?: 0
            val newUsedPoints = currentUsedPoints + pointsRedeemed
            
            transaction.update(userRef, "used_points", newUsedPoints)
            null
        }
    }
    
    private fun showRedeemSuccess(couponCode: String) {
        // Update UI
        successLayout.visibility = View.VISIBLE
        couponCodeText.text = couponCode
        
        // Play animation
        redeemSuccessAnimation.playAnimation()
        
        // Apply animations to views
        AnimationUtils.applyEntryAnimation(successLayout)
        AnimationUtils.sequentialFadeIn(
            listOf(
                redeemSuccessAnimation,
                couponCodeText,
                copyCodeButton,
                doneButton
            ),
            delayBetween = 200
        )
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Coupon Code", text)
        clipboard?.setPrimaryClip(clip)
        
        Toast.makeText(context, "Coupon code copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    private fun generateCouponCode(): String {
        val uuid = UUID.randomUUID().toString()
        val shortCode = uuid.substring(0, 8).uppercase(Locale.getDefault())
        return "${coupon.partnerName.substring(0, 2).uppercase()}-$shortCode"
    }
    
    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }
} 