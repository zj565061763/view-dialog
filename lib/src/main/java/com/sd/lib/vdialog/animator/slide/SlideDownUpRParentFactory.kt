package com.sd.lib.vdialog.animator.slide

import android.view.View

/**
 * Slide relative to the view parent.
 *
 * show: slide down
 *
 * hide: slide up
 */
class SlideDownUpRParentFactory : SlideVerticalFactory() {
    override fun getValueHidden(view: View): Float {
        return (-view.bottom).toFloat()
    }

    override fun getValueShown(view: View): Float {
        return 0f
    }
}