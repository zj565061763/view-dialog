package com.sd.lib.vdialog

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.sd.lib.vdialog.animator.AnimatorFactory

interface IDialog {
    /**
     * Whether to enable debug mode.(Log tag：view-dialog)
     */
    var isDebug: Boolean

    /**
     * [Context]
     */
    val context: Context

    /**
     * [Activity]
     */
    val ownerActivity: Activity

    /**
     * [Display]
     */
    var display: Display

    /**
     * The animator duration of the dialog, it is enabled when the value is greater than 0.
     * The default value is 0.
     */
    var animatorDuration: Long

    /**
     * [AnimatorFactory]
     */
    var animatorFactory: AnimatorFactory?

    /**
     * Describes how the dialog are positioned.
     * The default value is [Gravity.CENTER].
     */
    var gravity: Int

    /**
     * Whether the dialog has a dim background.
     * The default value is true.
     */
    var isBackgroundDim: Boolean

    /**
     * [Padding]
     */
    val padding: Padding

    /**
     * Whether the dialog is currently showing.
     */
    val isShowing: Boolean

    /**
     * The dialog content view.
     */
    val contentView: View?

    /**
     * Set the dialog content from a layout resource.
     */
    fun setContentView(resId: Int)

    /**
     * Set the dialog content to an explicit view.
     */
    fun setContentView(view: View?)

    /**
     * Finds the first descendant view with the given [id].
     */
    fun <T : View> findViewById(id: Int): T?

    /**
     * Sets whether this dialog is cancelable with the [KeyEvent.KEYCODE_BACK] or [KeyEvent.KEYCODE_ESCAPE] key.
     * The default value is true.
     */
    fun setCancelable(cancel: Boolean)

    /**
     * Sets whether the dialog is canceled when touched outside the content's bounds.
     * If setting to true, the dialog is set to be cancelable if not already set.
     * The default value is true.
     */
    fun setCanceledOnTouchOutside(cancel: Boolean)

    /**
     * [OnDismissListener]
     */
    fun setOnDismissListener(listener: OnDismissListener?)

    /**
     * [OnShowListener]
     */
    fun setOnShowListener(listener: OnShowListener?)

    /**
     * [OnCancelListener]
     */
    fun setOnCancelListener(listener: OnCancelListener?)

    /**
     * Show the dialog, This method can be called safely from any thread.
     */
    fun show()

    /**
     * Dismiss the dialog, removing it from the screen. This method can be called safely from any thread.
     */
    fun dismiss()

    /**
     * Cancel the dialog. This is essentially the same as calling [dismiss],
     * but it will also call your [OnCancelListener] (if registered).
     */
    fun cancel()

    /**
     * Interface used to run some code when the dialog is dismissed.
     */
    fun interface OnDismissListener {
        /**
         * Called when the dialog is dismissed.
         */
        fun onDismiss(dialog: IDialog)
    }

    /**
     * Interface used to run some code when the dialog is shown.
     */
    fun interface OnShowListener {
        /**
         * Called when the dialog is shown.
         */
        fun onShow(dialog: IDialog)
    }

    /**
     * Interface used to run some code when the dialog is canceled.
     */
    fun interface OnCancelListener {
        /**
         * Called when the dialog is canceled.
         */
        fun onCancel(dialog: IDialog)
    }

    /**
     * Interface used to display the dialog view in a specified container.
     */
    interface Display {
        /**
         * Add the dialog view to a container.
         */
        fun addView(view: View)

        /**
         * Remove the dialog view from container.
         */
        fun removeView(view: View)
    }

    interface Padding {
        /**
         * Returns the left padding of the dialog in pixels.
         */
        val left: Int

        /**
         * Returns the top padding of the dialog in pixels.
         */
        val top: Int

        /**
         * Returns the right padding of the dialog in pixels.
         */
        val right: Int

        /**
         * Returns the bottom padding of the dialog in pixels.
         */
        val bottom: Int

        /**
         * Sets the padding.
         *
         * @param left the left padding in pixels
         * @param top the top padding in pixels
         * @param right the right padding in pixels
         * @param bottom the right padding in pixels
         */
        fun set(left: Int, top: Int, right: Int, bottom: Int)
    }
}