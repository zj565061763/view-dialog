package com.sd.demo.view_dialog

import android.content.Context
import android.view.View
import com.sd.lib.vdialog.FDialog

class SimpleDialog(context: Context) : FDialog(context) {

    override fun onCreate() {
        super.onCreate()
        logMsg { "onCreate" }
        setContentView(R.layout.dialog_simple)
        findViewById<View>(R.id.btn)?.setOnClickListener {
            dismiss()
        }
    }

    override fun onContentViewChanged(oldView: View?, newView: View?) {
        super.onContentViewChanged(oldView, newView)
        logMsg { "onContentViewChanged $oldView -> $newView" }
    }

    override fun onStart() {
        super.onStart()
        logMsg { "onStart" }
    }

    override fun onStop() {
        super.onStop()
        logMsg { "onStop" }
    }

    init {
        // Enable debug mode.(Log tag：view-dialog)
        isDebug = true

        // Set the padding.
        padding.set(0, 0, 0, 0)

        setCanceledOnTouchOutside(true)
    }
}