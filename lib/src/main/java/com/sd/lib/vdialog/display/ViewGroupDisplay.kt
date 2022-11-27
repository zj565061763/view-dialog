package com.sd.lib.vdialog.display

import android.view.View
import android.view.ViewGroup
import com.sd.lib.vdialog.IDialog

class ViewGroupDisplay(viewGroup: ViewGroup) : IDialog.Display {
    private val _viewGroup = viewGroup

    override fun addView(view: View) {
        _viewGroup.addView(
            view,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun removeView(view: View) {
        _viewGroup.removeView(view)
    }
}