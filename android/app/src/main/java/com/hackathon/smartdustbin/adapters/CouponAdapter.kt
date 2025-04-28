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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CouponAdapter(
    private var coupons: ArrayList<Coupon>,
    private val onCouponClick: (Coupon) -> Unit
) : RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
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

    inner class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val couponImage: ImageView = itemView.findViewById(R.id.couponImage)
        private val couponTitle: TextView = itemView.findViewById(R.id.couponTitle)
        private val couponDescription: TextView = itemView.findViewById(R.id.couponDescription)
        private val pointsRequired: TextView = itemView.findViewById(R.id.pointsRequired)
        private val validityText: TextView = itemView.findViewById(R.id.validityText)

        fun bind(coupon: Coupon) {
            // Set coupon data
            couponTitle.text = coupon.title
            couponDescription.text = coupon.description
            pointsRequired.text = "${coupon.pointsRequired} points"
            
            // Format validity date
            val validityDate = coupon.endDate?.toDate()
            validityText.text = "Valid till ${formatDate(validityDate)}"
            
            // Load image with Glide
            Glide.with(itemView.context)
                .load(coupon.imageUrl)
                .placeholder(R.drawable.placeholder_coupon)
                .error(R.drawable.placeholder_coupon)
                .centerCrop()
                .into(couponImage)
            
            // Apply background color based on partner category
            when (coupon.category) {
                "food_drinks" -> {
                    couponImage.setBackgroundResource(R.drawable.circle_background)
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorFoodDrinks)
                }
                "shopping" -> {
                    couponImage.setBackgroundResource(R.drawable.circle_background)
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorShopping)
                }
                "entertainment" -> {
                    couponImage.setBackgroundResource(R.drawable.circle_background)
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorEntertainment)
                }
                else -> {
                    couponImage.setBackgroundResource(R.drawable.circle_background)
                    couponImage.backgroundTintList = itemView.context.getColorStateList(R.color.colorPrimary)
                }
            }
        }
        
        private fun formatDate(date: Date?): String {
            if (date == null) return "N/A"
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            return sdf.format(date)
        }
    }
}