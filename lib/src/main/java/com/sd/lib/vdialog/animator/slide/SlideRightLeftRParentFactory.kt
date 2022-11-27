package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view parent.
 *
 * show: slide to right
 *
 * hide: slide to left
 */
class SlideRightLeftRParentFactory : SlideHorizontalFactory() {
    override fun getValueHidden(view: View): Float {
        return (-view.right).toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}