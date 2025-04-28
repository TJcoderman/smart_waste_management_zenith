package com.hackathon.smartdustbin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.models.Deposit
import com.hackathon.smartdustbin.utils.AnimationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HistoryAdapter(
    private var deposits: ArrayList<Deposit>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val binLocations = HashMap<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val deposit = deposits[position]
        holder.bind(deposit)
        
        // Apply entry animation with staggered delay
        AnimationUtils.applyEntryAnimation(holder.itemView, position * 50)
    }

    override fun getItemCount(): Int = deposits.size

    fun updateData(newDeposits: ArrayList<Deposit>) {
        this.deposits = newDeposits
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wasteTypeIcon: ImageView = itemView.findViewById(R.id.wasteTypeIcon)
        private val wasteTypeText: TextView = itemView.findViewById(R.id.wasteTypeText)
        private val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        private val binLocationText: TextView = itemView.findViewById(R.id.binLocationText)
        private val weightText: TextView = itemView.findViewById(R.id.weightText)
        private val pointsText: TextView = itemView.findViewById(R.id.pointsText)

        fun bind(deposit: Deposit) {
            // Set waste type icon and text
            when (deposit.waste_type) {
                "organic" -> {
                    wasteTypeIcon.setImageResource(R.drawable.ic_organic)
                    wasteTypeIcon.backgroundTintList = itemView.context.getColorStateList(R.color.colorOrganic)
                    wasteTypeText.text = "Organic Waste"
                }
                "recyclable_plastic" -> {
                    wasteTypeIcon.setImageResource(R.drawable.ic_plastic)
                    wasteTypeIcon.backgroundTintList = itemView.context.getColorStateList(R.color.colorRecyclable)
                    wasteTypeText.text = "Plastic"
                }
                "recyclable_paper" -> {
                    wasteTypeIcon.setImageResource(R.drawable.ic_paper)
                    wasteTypeIcon.backgroundTintList = itemView.context.getColorStateList(R.color.colorRecyclable)
                    wasteTypeText.text = "Paper"
                }
                "recyclable_metal" -> {
                    wasteTypeIcon.setImageResource(R.drawable.ic_metal)
                    wasteTypeIcon.backgroundTintList = itemView.context.getColorStateList(R.color.colorRecyclable)
                    wasteTypeText.text = "Metal"
                }
                else -> {
                    wasteTypeIcon.setImageResource(R.drawable.ic_recycle)
                    wasteTypeIcon.backgroundTintList = itemView.context.getColorStateList(R.color.colorPrimary)
                    wasteTypeText.text = "Unknown Type"
                }
            }
            
            // Set timestamp
            timestampText.text = formatTimestamp(deposit.timestamp)
            
            // Set weight
            weightText.text = "${String.format("%.1f", deposit.weight)} kg"
            
            // Set points
            pointsText.text = "${deposit.points_earned} points"
            
            // Set bin location (fetch from Firestore if not cached)
            setBinLocation(deposit.bin_id)
        }
        
        private fun formatTimestamp(timestamp: Timestamp?): String {
            if (timestamp == null) return "Unknown time"
            
            val date = timestamp.toDate()
            val now = Calendar.getInstance().time
            val diffInMillis = now.time - date.time
            val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            
            return when {
                diffInHours < 1 -> "Just now"
                diffInHours < 24 -> "${diffInHours.toInt()} hour${if (diffInHours > 1) "s" else ""} ago"
                diffInDays < 2 -> "Yesterday, ${formatTime(date)}"
                diffInDays < 7 -> formatDayOfWeek(date)
                else -> formatFullDate(date)
            }
        }
        
        private fun formatTime(date: Date): String {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(date)
        }
        
        private fun formatDayOfWeek(date: Date): String {
            val sdf = SimpleDateFormat("EEEE, h:mm a", Locale.getDefault())
            return sdf.format(date)
        }
        
        private fun formatFullDate(date: Date): String {
            val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            return sdf.format(date)
        }
        
        private fun setBinLocation(binId: String?) {
            if (binId == null) {
                binLocationText.text = "Unknown location"
                return
            }
            
            // Check if location is cached
            if (binLocations.containsKey(binId)) {
                binLocationText.text = binLocations[binId]
                return
            }
            
            // Fetch from Firestore
            binLocationText.text = "Loading location..."
            db.collection("smart_bins").document(binId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val location = document.getString("location") ?: "Unknown location"
                        binLocations[binId] = "Bin at $location"
                        binLocationText.text = binLocations[binId]
                    } else {
                        binLocationText.text = "Unknown location"
                    }
                }
                .addOnFailureListener {
                    binLocationText.text = "Unknown location"
                }
        }
    }
}