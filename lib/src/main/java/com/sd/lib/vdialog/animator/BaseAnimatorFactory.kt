package com.sd.lib.vdialog.animator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

abstract class BaseAnimatorFactory : AnimatorFactory {
    override fun createAnimator(show: Boolean, view: View): Animator? {
        return createAnimatorImpl(show, view)?.also {
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    this@BaseAnimatorFactory.onAnimationStart(show, view)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    animation.removeListener(this)
                    this@BaseAnimatorFactory.onAnimationEnd(show, view)
                }
            })
        }
    }

    protected abstract fun createAnimatorImpl(show: Boolean, view: View): Animator?

    protected open fun onAnimationStart(show: Boolean, view: View) {}

    protected open fun onAnimationEnd(show: Boolean, view: View) {}
}