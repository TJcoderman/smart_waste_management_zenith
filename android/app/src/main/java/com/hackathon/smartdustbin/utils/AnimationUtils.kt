package com.hackathon.smartdustbin.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Utility class for animations throughout the app
 */
object AnimationUtils {

    /**
     * Apply entry animation to a view with fade and slight translation
     */
    fun applyEntryAnimation(view: View, delay: Long = 0) {
        view.alpha = 0f
        view.translationY = 50f
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(delay)
            .start()
    }
    
    /**
     * Apply click animation that gives feedback through a slight scale change
     */
    fun applyClickAnimation(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
            .start()
    }
    
    /**
     * Apply a pulsing animation that repeats indefinitely
     */
    fun applyPulseAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.1f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 1000
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.repeatCount = ValueAnimator.INFINITE
        animatorSet.start()
    }
    
    /**
     * Animate a number counter in a TextView
     */
    fun animateCounter(textView: TextView, from: Int, to: Int, duration: Long = 1500, format: String = "%d") {
        val animator = ValueAnimator.ofInt(from, to)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            textView.text = String.format(format, value)
        }
        animator.start()
    }
    
    /**
     * Animate a floating point counter in a TextView
     */
    fun animateFloatCounter(textView: TextView, from: Float, to: Float, duration: Long = 1500, format: String = "%.1f") {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            textView.text = String.format(format, value)
        }
        animator.start()
    }
    
    /**
     * Animate progress bar from current to target value
     */
    fun animateProgressBar(progressBar: ProgressBar, to: Int, duration: Long = 1000) {
        val from = progressBar.progress
        val animator = ObjectAnimator.ofInt(progressBar, "progress", from, to)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    
    /**
     * Sequentially fade in multiple views with a delay between each
     */
    fun sequentialFadeIn(views: List<View>, delayBetween: Long = 100, duration: Long = 500) {
        for (i in views.indices) {
            val view = views[i]
            view.alpha = 0f
            
            Handler(Looper.getMainLooper()).postDelayed({
                view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, i * delayBetween)
        }
    }
    
    /**
     * Apply a shake animation to indicate error or warning
     */
    fun applyShakeAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    
    /**
     * Apply a bouncing animation that draws attention to a view
     */
    fun applyBounceAnimation(view: View, repeatCount: Int = 3) {
        val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -30f, 0f)
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.repeatCount = repeatCount
        animator.start()
    }
    
    /**
     * Flash a view by briefly changing its alpha
     */
    fun applyFlashAnimation(view: View, duration: Long = 300) {
        val originalAlpha = view.alpha
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, originalAlpha, 0.3f, originalAlpha)
        animator.duration = duration
        animator.repeatCount = 1
        animator.start()
    }
}