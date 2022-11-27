package com.sd.lib.vdialog.animator

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View

interface AnimatorFactory {
    /**
     * Create animator for the [view].
     *
     * Note that the dismiss animation cannot be set to infinite loop, otherwise the dialog will not be removed.
     *
     * @param show true: show dialog, false: dismiss dialog
     * @param view the dialog content view
     */
    fun createAnimator(show: Boolean, view: View): Animator?
}

operator fun AnimatorFactory.plus(factory: AnimatorFactory): AnimatorFactory {
    return PlusFactory(this, factory)
}

private class PlusFactory(
    private val factoryA: AnimatorFactory,
    private val factoryB: AnimatorFactory,
) : AnimatorFactory {

    override fun createAnimator(show: Boolean, view: View): Animator? {
        val animatorA = factoryA.createAnimator(show, view)
        val animatorB = factoryB.createAnimator(show, view)
        return if (animatorA != null && animatorB != null) {
            AnimatorSet().apply {
                this.play(animatorA).with(animatorB)
            }
        } else {
            animatorA ?: animatorB
        }
    }
}