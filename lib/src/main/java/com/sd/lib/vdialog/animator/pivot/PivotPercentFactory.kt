package com.sd.lib.vdialog.animator.pivot

import android.animation.Animator
import android.view.View
import com.sd.lib.vdialog.animator.AnimatorFactory
import com.sd.lib.vdialog.animator.BaseAnimatorFactory

/**
 *  This factory is usually used together with scale animator factory.
 *
 *  View's pivotX and pivotY will be modified when animation start and restore when animation ends.
 */
class PivotPercentFactory(
    factory: AnimatorFactory,
    pivotPercentX: Float,
    pivotPercentY: Float,
) : BaseAnimatorFactory() {
    private val _pivotPercentX = pivotPercentX
    private val _pivotPercentY = pivotPercentY
    private val _factory = PivotFactory(
        factory,
        { _, view -> _pivotPercentX * view.width },
        { _, view -> _pivotPercentY * view.height }
    )

    override fun createAnimatorImpl(show: Boolean, view: View): Animator? {
        return _factory.createAnimator(show, view)
    }
}