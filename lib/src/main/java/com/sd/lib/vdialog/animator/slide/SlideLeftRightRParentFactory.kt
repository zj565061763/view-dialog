package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view parent.
 *
 * show: slide to left
 *
 * hide: slide to right
 */
class SlideLeftRightRParentFactory : SlideHorizontalFactory() {
    override fun getValueHidden(view: View): Float {
        val parent = view.parent
        if (parent is View) {
            val distance = parent.width - view.left
            if (distance > 0) return distance.toFloat()
        }
        return view.width.toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}