package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view parent.
 *
 * show: slide up
 *
 * hide: slide down
 */
class SlideUpDownRParentFactory : SlideVerticalFactory() {
    override fun getValueHidden(view: View): Float {
        val parent = view.parent
        if (parent is View) {
            val distance = parent.height - view.top
            if (distance > 0) return distance.toFloat()
        }
        return view.height.toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}