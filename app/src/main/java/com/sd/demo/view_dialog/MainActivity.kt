package com.sd.demo.view_dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.view_dialog.databinding.ActivityMainBinding
import com.sd.lib.vdialog.FDialog
import com.sd.lib.vdialog.animator.AnimatorFactory
import com.sd.lib.vdialog.animator.plus
import com.sd.lib.vdialog.animator.scale.ScaleXYFactory
import com.sd.lib.vdialog.animator.slide.SlideUpDownRParentFactory

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
    }

    private fun createDialog(): FDialog {
        val dialog = SimpleDialog(this)

        dialog.setOnDismissListener {
            logMsg { "OnDismissListener" }
        }

        dialog.setOnShowListener {
            logMsg { "OnShowListener" }
        }

        dialog.setOnCancelListener {
            logMsg { "OnCancelListener" }
        }

        /**
         * Set the [AnimatorFactory] for the dialog.
         */
        dialog.animatorFactory = ScaleXYFactory() + SlideUpDownRParentFactory()

        // Set the animator duration for the dialog.
        dialog.animatorDuration = 1000

        return dialog
    }

    override fun onClick(v: View) {
        when (v) {
            _binding.btnSimple -> {
                createDialog().show()
            }
            _binding.btnSimpleTwo -> {
                createDialog().show()
                v.postDelayed({
                    createDialog().show()
                }, 1000)
            }
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("view-dialog-demo", block())
}