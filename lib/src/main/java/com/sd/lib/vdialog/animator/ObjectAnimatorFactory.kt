package com.sd.lib.vdialog.animator

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import kotlin.math.abs

abstract class ObjectAnimatorFactory : BaseAnimatorFactory() {
    override fun createAnimatorImpl(show: Boolean, view: View): Animator? {
        val animator = ObjectAnimator()
        animator.setPropertyName(getPropertyName())

        val valueHidden = getValueHidden(view)
        val valueShown = getValueShown(view)

        val values = if (show) {
            floatArrayOf(valueHidden, valueShown)
        } else {
            floatArrayOf(getValueCurrent(view), valueHidden)
        }
        animator.setFloatValues(*values)

        val duration = getScaledDuration(
            value = values[0] - values[1],
            maxValue = valueShown - valueHidden,
            maxDuration = getDuration(show, view),
        )

        animator.duration = duration
        animator.target = view
        return animator
    }

    /**
     * [ObjectAnimator.setPropertyName]
     */
    protected abstract fun getPropertyName(): String

    /**
     * Returns the value when the view is hidden.
     */
    protected abstract fun getValueHidden(view: View): Float

    /**
     * Returns the value when the view is shown.
     */
    protected abstract fun getValueShown(view: View): Float

    /**
     * Returns the currently value of the view.
     */
    protected abstract fun getValueCurrent(view: View): Float

    /**
     * [ObjectAnimator.setDuration]
     */
    protected open fun getDuration(show: Boolean, view: View): Long {
        return 200
    }

    companion object {
        private fun getScaledDuration(
            value: Float,
            maxValue: Float,
            maxDuration: Long
        ): Long {
            if (value == 0f) return 0
            if (maxValue == 0f) return 0
            if (maxDuration <= 0) return 0
            val percent = abs(value / maxValue)
            val duration = (percent * maxDuration).toLong()
            return duration.coerceAtMost(maxDuration)
        }
    }
}