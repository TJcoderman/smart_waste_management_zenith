package com.hackathon.smartdustbin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.models.Coupon
import com.hackathon.smartdustbin.utils.AnimationUtils
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class RewardsAdapter(
    private var coupons: ArrayList<Coupon>,
    private val isFeatured: Boolean = false,
    private val onCouponClick: (Coupon) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val layoutId = if (isFeatured) R.layout.item_featured_reward else R.layout.item_reward
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val coupon = coupons[position]
        holder.bind(coupon)
        
        // Apply entry animation with staggered delay
        AnimationUtils.applyEntryAnimation(holder.itemView, position * 50)
        
        // Setup click listener
        holder.itemView.setOnClickListener {
            AnimationUtils.applyClickAnimation(it)
            onCouponClick(coupon)
        }
    }

    override fun getItemCount(): Int = coupons.size

    fun updateData(newCoupons: ArrayList<Coupon>) {
        this.coupons = newCoupons
        notifyDataSetChanged()
    }

    inner class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        fun bind(coupon: Coupon) {
            if (isFeatured) {
                bindFeaturedCoupon(coupon)
            } else {
                bindRegularCoupon(coupon)
            }
        }
        
        private fun bindFeaturedCoupon(coupon: Coupon) {
            val featuredImage: ImageView = itemView.findViewById(R.id.featuredImage)
            val partnerBadge: TextView = itemView.findViewById(R.id.partnerBadge)
            val featuredTitle: TextView = itemView.findViewById(R.id.featuredTitle)
            val featuredPoints: TextView = itemView.findViewById(R.id.featuredPoints)
            val featuredNew: TextView = itemView.findViewById(R.id.featuredNew)
            
            // Set data
            featuredTitle.text = coupon.title
            featuredPoints.text = "${coupon.pointsRequired} points"
            partnerBadge.text = coupon.partnerName
            
            // Show/hide NEW badge based on creation date
            val isNew = isNewCoupon(coupon.startDate?.toDate())
            featuredNew.visibility = if (isNew) View.VISIBLE else View.GONE
            
            // Load image with Glide
            Glide.with(itemView.context)
                .load(coupon.imageUrl)
                .placeholder(R.drawable.placeholder_featured)
                .error(R.drawable.placeholder_featured)
                .centerCrop()
                .into(featuredImage)
        }
        
        private fun bindRegularCoupon(coupon: Coupon) {
            val couponImage: ImageView = itemView.findViewById(R.id.couponImage)
            val couponTitle: TextView = itemView.findViewById(R.id.couponTitle)
            val couponDescription: TextView = itemView.findViewById(R.id.couponDescription)
            val pointsRequired: TextView = itemView.findViewById(R.id.pointsRequired)
            val validityText: TextView = itemView.findViewById(R.id.validityText)
            
            // Set data
            couponTitle.text = coupon.title
            couponDescription.text = coupon.description
            pointsRequired.text = "${coupon.pointsRequired} points"
            
            // Format validity text
            val endDate = coupon.endDate?.toDate()
            if (endDate != null) {
                val now = Calendar.getInstance().time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(endDate.time - now.time)
                
                validityText.text = when {
                    diffInDays < 0 -> "Expired"
                    diffInDays == 0L -> "Expires today"
                    diffInDays == 1L -> "Expires tomorrow"
                    diffInDays < 7 -> "Expires in $diffInDays days"
                    else -> "Valid till ${formatShortDate(endDate)}"
                }
                
                // Highlight expiring soon
                if (diffInDays in 0..2) {
                    validityText.setTextColor(itemView.context.getColor(R.color.colorWarning))
                }
            } else {
                validityText.text = "No expiry date"
            }
            
            // Load partner logo
            Glide.with(itemView.context)
                .load(coupon.partnerLogoUrl)
                .placeholder(R.drawable.placeholder_logo)
                .error(R.drawable.placeholder_logo)
                .centerCrop()
                .into(couponImage)
            
            // Apply background tint based on category
            when (coupon.category) {
                "food_drinks" -> {
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorFoodDrinks)
                }
                "shopping" -> {
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorShopping)
                }
                "entertainment" -> {
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorEntertainment)
                }
                else -> {
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorPrimary)
                }
            }
        }
        
        private fun isNewCoupon(startDate: Date?): Boolean {
            if (startDate == null) return false
            
            val now = Calendar.getInstance().time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(now.time - startDate.time)
            return diffInDays <= 7 // Consider new if added within the last 7 days
        }
        
        private fun formatShortDate(date: Date): String {
            val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault())
            return sdf.format(date)
        }
    }
}