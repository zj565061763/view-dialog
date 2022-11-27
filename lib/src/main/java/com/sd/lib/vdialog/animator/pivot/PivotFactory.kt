package com.sd.lib.vdialog.animator.pivot

import android.animation.Animator
import android.view.View
import com.sd.lib.vdialog.animator.AnimatorFactory
import com.sd.lib.vdialog.animator.BaseAnimatorFactory
import com.sd.lib.vdialog.animator.pivot.PivotFactory.PivotProvider

/**
 *  This factory is usually used together with scale animator factory.
 *
 *  View's pivotX and pivotY will be modified when animation start and restore when animation ends.
 */
class PivotFactory(
    factory: AnimatorFactory,
    pivotProviderX: PivotProvider?,
    pivotProviderY: PivotProvider?,
) : BaseAnimatorFactory() {
    private val _factory = factory
    private val _pivotHolder = PivotHolder()

    private val _pivotProviderX = pivotProviderX ?: PivotProvider { _, view -> view.pivotX }
    private val _pivotProviderY = pivotProviderY ?: PivotProvider { _, view -> view.pivotY }

    override fun createAnimatorImpl(show: Boolean, view: View): Animator? {
        return _factory.createAnimator(show, view)
    }

    override fun onAnimationStart(show: Boolean, view: View) {
        super.onAnimationStart(show, view)
        _pivotHolder.setPivotXY(
            pivotX = _pivotProviderX.getPivot(show, view),
            pivotY = _pivotProviderY.getPivot(show, view),
            view = view
        )
    }

    override fun onAnimationEnd(show: Boolean, view: View) {
        super.onAnimationEnd(show, view)
        _pivotHolder.restore(view)
    }

    private class PivotHolder {
        private val _pivotXYBackup = FloatArray(2)

        fun setPivotXY(
            pivotX: Float,
            pivotY: Float,
            view: View
        ) {
            _pivotXYBackup[0] = view.pivotX
            _pivotXYBackup[1] = view.pivotY

            view.pivotX = pivotX
            view.pivotY = pivotY
        }

        fun restore(view: View) {
            view.pivotX = _pivotXYBackup[0]
            view.pivotY = _pivotXYBackup[1]
        }
    }

    fun interface PivotProvider {
        fun getPivot(show: Boolean, view: View): Float
    }
}