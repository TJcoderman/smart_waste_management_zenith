package com.hackathon.smartdustbin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.hackathon.smartdustbin.R
import com.hackathon.smartdustbin.utils.AnimationUtils
import com.hackathon.smartdustbin.utils.Constants
import com.hackathon.smartdustbin.utils.FirebaseUtils
import java.util.ArrayList

class ImpactFragment : Fragment() {

    private lateinit var impactCard: CardView
    private lateinit var statisticsCard: CardView
    private lateinit var leaderboardCard: CardView
    private lateinit var earthAnimation: LottieAnimationView
    private lateinit var co2SavedText: TextView
    private lateinit var treesPlantedText: TextView
    private lateinit var waterSavedText: TextView
    private lateinit var energySavedText: TextView
    private lateinit var wasteTypeChart: PieChart
    private lateinit var totalRecycledText: TextView
    private lateinit var organicText: TextView
    private lateinit var plasticText: TextView
    private lateinit var paperText: TextView
    private lateinit var metalText: TextView
    private lateinit var rankText: TextView
    private lateinit var cityRankText: TextView
    private lateinit var communityImpactText: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_impact, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initViews(view)
        
        // Apply animations
        setupAnimations()
        
        // Load user impact data
        loadImpactData()
        
        // Setup pie chart
        setupWasteTypeChart()
        
        return view
    }
    
    private fun initViews(view: View) {
        impactCard = view.findViewById(R.id.impactCard)
        statisticsCard = view.findViewById(R.id.statisticsCard)
        leaderboardCard = view.findViewById(R.id.leaderboardCard)
        earthAnimation = view.findViewById(R.id.earthAnimation)
        co2SavedText = view.findViewById(R.id.co2SavedText)
        treesPlantedText = view.findViewById(R.id.treesPlantedText)
        waterSavedText = view.findViewById(R.id.waterSavedText)
        energySavedText = view.findViewById(R.id.energySavedText)
        wasteTypeChart = view.findViewById(R.id.wasteTypeChart)
        totalRecycledText = view.findViewById(R.id.totalRecycledText)
        organicText = view.findViewById(R.id.organicText)
        plasticText = view.findViewById(R.id.plasticText)
        paperText = view.findViewById(R.id.paperText)
        metalText = view.findViewById(R.id.metalText)
        rankText = view.findViewById(R.id.rankText)
        cityRankText = view.findViewById(R.id.cityRankText)
        communityImpactText = view.findViewById(R.id.communityImpactText)
    }
    
    private fun setupAnimations() {
        // Play earth animation
        earthAnimation.playAnimation()
        
        // Apply entry animations with staggered delay
        AnimationUtils.applyEntryAnimation(impactCard, 100)
        AnimationUtils.applyEntryAnimation(statisticsCard, 200)
        AnimationUtils.applyEntryAnimation(leaderboardCard, 300)
        
        // Apply click animations
        arrayOf(impactCard, statisticsCard, leaderboardCard).forEach { card ->
            card.setOnClickListener {
                AnimationUtils.applyClickAnimation(it)
            }
        }
    }
    
    private fun loadImpactData() {
        val userId = auth.currentUser?.uid ?: return
        
        // Get waste statistics from Firebase
        FirebaseUtils.getWasteStatistics(
            userId,
            onSuccess = { stats ->
                updateImpactUI(stats)
            },
            onFailure = { exception ->
                // Handle error
                println("Error: ${exception.message}")
            }
        )
    }
    
    private fun updateImpactUI(stats: Map<String, Any>) {
        // Extract statistics
        val organicTotal = stats["organicTotal"] as? Double ?: 0.0
        val plasticTotal = stats["plasticTotal"] as? Double ?: 0.0
        val paperTotal = stats["paperTotal"] as? Double ?: 0.0
        val metalTotal = stats["metalTotal"] as? Double ?: 0.0
        val recyclableTotal = stats["recyclableTotal"] as? Double ?: 0.0
        val co2Saved = stats["co2Saved"] as? Double ?: 0.0
        val treesSaved = stats["treesSaved"] as? Double ?: 0.0
        
        // Update total recycled text
        val totalRecycled = organicTotal + recyclableTotal
        AnimationUtils.animateFloatCounter(
            totalRecycledText,
            0f,
            totalRecycled.toFloat(),
            format = "%.1f kg"
        )
        
        // Update individual waste type texts
        AnimationUtils.animateFloatCounter(organicText, 0f, organicTotal.toFloat(), format = "%.1f kg")
        AnimationUtils.animateFloatCounter(plasticText, 0f, plasticTotal.toFloat(), format = "%.1f kg")
        AnimationUtils.animateFloatCounter(paperText, 0f, paperTotal.toFloat(), format = "%.1f kg")
        AnimationUtils.animateFloatCounter(metalText, 0f, metalTotal.toFloat(), format = "%.1f kg")
        
        // Update environmental impact metrics
        AnimationUtils.animateFloatCounter(co2SavedText, 0f, co2Saved.toFloat(), format = "%.1f kg")
        AnimationUtils.animateFloatCounter(treesPlantedText, 0f, treesSaved.toFloat(), format = "%.2f trees")
        
        // Calculate additional environmental impacts (approximations)
        val waterSaved = recyclableTotal * 13.0 // Liters of water saved per kg recycled (approximation)
        val energySaved = recyclableTotal * 5.0 // kWh of energy saved per kg recycled (approximation)
        
        AnimationUtils.animateFloatCounter(waterSavedText, 0f, waterSaved.toFloat(), format = "%.0f L")
        AnimationUtils.animateFloatCounter(energySavedText, 0f, energySaved.toFloat(), format = "%.1f kWh")
        
        // Update pie chart data
        updatePieChartData(organicTotal, plasticTotal, paperTotal, metalTotal)
        
        // Placeholder for community rank (would come from server in real app)
        rankText.text = "#15"
        cityRankText.text = "Top 10%"
        
        // Calculate community impact text
        val communityMessageIndex = (totalRecycled / 5).toInt().coerceAtMost(communityMessages.size - 1)
        communityImpactText.text = communityMessages[communityMessageIndex]
    }
    
    private fun setupWasteTypeChart() {
        // Configure pie chart appearance
        wasteTypeChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.WHITE)
            holeRadius = 58f
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            legend.isEnabled = false
            setTouchEnabled(false)
            animateY(1400)
        }
    }
    
    private fun updatePieChartData(organic: Double, plastic: Double, paper: Double, metal: Double) {
        val entries = ArrayList<PieEntry>()
        
        // Add entries only if they have values
        if (organic > 0) entries.add(PieEntry(organic.toFloat(), "Organic"))
        if (plastic > 0) entries.add(PieEntry(plastic.toFloat(), "Plastic"))
        if (paper > 0) entries.add(PieEntry(paper.toFloat(), "Paper"))
        if (metal > 0) entries.add(PieEntry(metal.toFloat(), "Metal"))
        
        // If no data, add placeholder
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }
        
        val dataSet = PieDataSet(entries, "Waste Types")
        
        // Set colors
        dataSet.colors = listOf(
            resources.getColor(R.color.colorOrganic, null),
            resources.getColor(R.color.colorPlastic, null),
            resources.getColor(R.color.colorPaper, null),
            resources.getColor(R.color.colorMetal, null)
        )
        
        val data = PieData(dataSet)
        data.setValueTextSize(0f) // Hide values
        
        wasteTypeChart.data = data
        wasteTypeChart.invalidate() // Refresh
    }
    
    // Motivational community impact messages
    private val communityMessages = listOf(
        "Start your eco-journey! Every deposit makes a difference.",
        "Keep going! Your actions inspire others in your community.",
        "Amazing work! You're becoming a waste reduction expert.",
        "Impressive! Your recycling is making a real difference.",
        "Extraordinary! You're in the top eco-warriors of your area.",
        "Outstanding! Your dedication is creating a cleaner future."
    )
}