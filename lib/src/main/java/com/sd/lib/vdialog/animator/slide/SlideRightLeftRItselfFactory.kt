package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view itself.
 *
 * show: slide to right
 *
 * hide: slide to left
 */
class SlideRightLeftRItselfFactory : SlideHorizontalFactory() {
    override fun getValueHidden(view: View): Float {
        return (-view.width).toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}