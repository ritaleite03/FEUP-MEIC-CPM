package com.example.generator.utils

import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd

fun expand(view: View) {
    view.measure(
        View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.UNSPECIFIED
    )
    val targetHeight = view.measuredHeight

    view.layoutParams.height = 0
    view.visibility = View.VISIBLE

    val animator = ValueAnimator.ofInt(0, targetHeight)
    animator.addUpdateListener { valueAnimator ->
        view.layoutParams.height = valueAnimator.animatedValue as Int
        view.requestLayout()
    }
    animator.duration = 300
    animator.start()
}

fun collapse(view: View) {
    val initialHeight = view.measuredHeight

    val animator = ValueAnimator.ofInt(initialHeight, 0)
    animator.addUpdateListener { valueAnimator ->
        view.layoutParams.height = valueAnimator.animatedValue as Int
        view.requestLayout()
    }

    animator.duration = 300
    animator.start()

    animator.doOnEnd {
        view.visibility = View.GONE
    }
}