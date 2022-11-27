package com.sd.lib.vdialog.animator.ext

import android.view.View
import com.sd.lib.vdialog.animator.ObjectAnimatorFactory

class AlphaFactory : ObjectAnimatorFactory() {
    override fun getPropertyName(): String {
        return View.ALPHA.name
    }

    override fun getValueHidden(view: View): Float {
        return 0.0f
    }

    override fun getValueShown(view: View): Float {
        return 1.0f
    }

    override fun getValueCurrent(view: View): Float {
        return view.alpha
    }
}