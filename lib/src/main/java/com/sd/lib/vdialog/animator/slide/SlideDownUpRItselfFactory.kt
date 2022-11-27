package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view itself.
 *
 * show: slide down
 *
 * hide: slide up
 */
class SlideDownUpRItselfFactory : SlideVerticalFactory() {
    override fun getValueHidden(view: View): Float {
        return (-view.height).toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}