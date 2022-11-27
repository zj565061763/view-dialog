package com.sd.lib.vdialog.animator.slide

import android.view.View
import com.sd.lib.vdialog.animator.ObjectAnimatorFactory

abstract class SlideHorizontalFactory : ObjectAnimatorFactory() {
    override fun getPropertyName(): String {
        return View.TRANSLATION_X.name
    }

    override fun getValueCurrent(view: View): Float {
        return view.translationX
    }
}