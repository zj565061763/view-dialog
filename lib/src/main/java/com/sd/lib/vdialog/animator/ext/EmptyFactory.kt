package com.sd.lib.vdialog.animator.ext

import android.animation.Animator
import android.view.View
import com.sd.lib.vdialog.animator.AnimatorFactory

class EmptyFactory : AnimatorFactory {
    override fun createAnimator(show: Boolean, view: View): Animator? {
        return null
    }
}