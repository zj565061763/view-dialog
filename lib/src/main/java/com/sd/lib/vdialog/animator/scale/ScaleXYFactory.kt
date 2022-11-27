package com.sd.lib.vdialog.animator.scale

import android.animation.Animator
import android.view.View
import com.sd.lib.vdialog.animator.AnimatorFactory
import com.sd.lib.vdialog.animator.plus

class ScaleXYFactory : AnimatorFactory {
    private val _factory by lazy { ScaleXFactory() + ScaleYFactory() }

    override fun createAnimator(show: Boolean, view: View): Animator? {
        return _factory.createAnimator(show, view)
    }
}