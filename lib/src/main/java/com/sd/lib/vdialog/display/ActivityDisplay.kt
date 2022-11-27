package com.sd.lib.vdialog.display

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.sd.lib.vdialog.IDialog

class ActivityDisplay : IDialog.Display {
    override fun addView(view: View) {
        val context = view.context
        if (context is Activity) {
            val container = context.findViewById<ViewGroup>(android.R.id.content)
            container.addView(view)
        }
    }

    override fun removeView(view: View) {
        val context = view.context
        if (context is Activity) {
            val container = context.findViewById<ViewGroup>(android.R.id.content)
            container.removeView(view)
        }
    }
}