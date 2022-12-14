package com.sd.lib.vdialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sd.lib.vdialog.animator.AnimatorFactory
import com.sd.lib.vdialog.animator.ObjectAnimatorFactory
import com.sd.lib.vdialog.animator.ext.AlphaFactory
import com.sd.lib.vdialog.animator.slide.SlideDownUpRParentFactory
import com.sd.lib.vdialog.animator.slide.SlideLeftRightRParentFactory
import com.sd.lib.vdialog.animator.slide.SlideRightLeftRParentFactory
import com.sd.lib.vdialog.animator.slide.SlideUpDownRParentFactory
import com.sd.lib.vdialog.display.ActivityDisplay
import com.sd.lib.vdialog.utils.FVisibilityAnimatorHandler
import java.util.*
import kotlin.properties.Delegates

open class FDialog(context: Context) : IDialog {
    private val _context = findContext(context)
    private val _dialogView = InternalDialogView(context)

    private var _contentView: View? = null
    private val backgroundView get() = _dialogView.backgroundView
    private val containerView get() = _dialogView.containerView

    private var _state = State.Dismiss
    private var _cancelable = true
    private var _canceledOnTouchOutside = true

    private var _isCreated = false
    private var _isStarted = false
    private var _isCanceled = false

    private var _isAnimatorFactoryModifiedInternal = false
    private var _showAnimatorFlag by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            logMsg(isDebug) { "_showAnimatorFlag $newValue ${this@FDialog}" }
        }
    }

    private var _onDismissListener: IDialog.OnDismissListener? = null
    private var _onShowListener: IDialog.OnShowListener? = null
    private var _onCancelListener: IDialog.OnCancelListener? = null

    private val _mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override var isDebug: Boolean = false

    override val context: Context get() = _context

    override val contentView: View? get() = _contentView

    override var display: IDialog.Display = ActivityDisplay()

    override fun setContentView(resId: Int) {
        val view = LayoutInflater.from(_context).inflate(resId, containerView, false)
        setContentView(view)
    }

    override fun setContentView(view: View?) {
        val old = _contentView
        if (old === view) return

        _contentView = view

        if (old != null) {
            containerView.removeView(old)
        }

        if (view != null) {
            val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams?.let {
                p.width = it.width
                p.height = it.height
            }
            containerView.addView(view, p)
        }

        onContentViewChanged(old, view)
    }

    override fun <T : View> findViewById(id: Int): T? {
        return _contentView?.findViewById(id)
    }

    override fun setCancelable(cancel: Boolean) {
        _cancelable = cancel
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        if (cancel) {
            _cancelable = true
        }
        _canceledOnTouchOutside = cancel
    }

    override fun setOnDismissListener(listener: IDialog.OnDismissListener?) {
        _onDismissListener = listener
    }

    override fun setOnShowListener(listener: IDialog.OnShowListener?) {
        _onShowListener = listener
    }

    override fun setOnCancelListener(listener: IDialog.OnCancelListener?) {
        _onCancelListener = listener
    }

    override var animatorDuration: Long = 0

    final override var animatorFactory: AnimatorFactory? by Delegates.observable(null) { _, _, _ ->
        _isAnimatorFactoryModifiedInternal = false
    }

    final override var gravity: Int by Delegates.observable(Gravity.NO_GRAVITY) { _, _, newValue ->
        containerView.gravity = newValue
    }

    final override var isBackgroundDim: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            val color = _context.resources.getColor(R.color.lib_view_dialog_background_dim)
            backgroundView.setBackgroundColor(color)
        } else {
            backgroundView.setBackgroundColor(0)
        }
    }

    final override val padding: IDialog.Padding by lazy {
        object : IDialog.Padding {
            override val left: Int get() = containerView.paddingLeft
            override val top: Int get() = containerView.paddingTop
            override val right: Int get() = containerView.paddingRight
            override val bottom: Int get() = containerView.paddingBottom
            override fun set(left: Int, top: Int, right: Int, bottom: Int) {
                containerView.setPadding(left, top, right, bottom)
            }
        }
    }

    override val isShowing: Boolean get() = _state == State.Show

    override fun show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showRunnable.run()
        } else {
            _mainHandler.removeCallbacks(_showRunnable)
            _mainHandler.post(_showRunnable)
        }
    }

    override fun dismiss() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _dismissRunnable.run()
        } else {
            _mainHandler.removeCallbacks(_dismissRunnable)
            _mainHandler.post(_dismissRunnable)
        }
    }

    override fun cancel() {
        _isCanceled = true
        dismiss()
    }

    private val _showRunnable = Runnable {
        if (_context.isFinishing()) return@Runnable
        if (_state.isShowPart) return@Runnable
        logMsg(isDebug) { "+++++ try show state:$_state ${this@FDialog}" }

        if (_animatorHandler.isHideAnimatorStarted) {
            logMsg(isDebug) { "cancel dismiss animator before show ${this@FDialog}" }
            _animatorHandler.cancelHideAnimator()
        }

        setState(State.TryShow)
        showDialog()
    }

    private val _dismissRunnable = Runnable {
        val isFinishing = _context.isFinishing()
        logMsg(isDebug) { "----- try dismiss state:$_state isFinishing:${isFinishing} ${this@FDialog}" }

        if (isFinishing) {
            _animatorHandler.cancelShowAnimator()
            _animatorHandler.cancelHideAnimator()
            dismissDialog()
            return@Runnable
        }

        if (_state.isDismissPart) {
            return@Runnable
        }

        if (_animatorHandler.isShowAnimatorStarted) {
            logMsg(isDebug) { "cancel show animator before dismiss ${this@FDialog}" }
            _animatorHandler.cancelShowAnimator()
        }

        setState(State.TryDismiss)
        _animatorHandler.setHideAnimator(createAnimator(false))
        if (_animatorHandler.startHideAnimator()) {
            // Dismiss dialog after animation end.
        } else {
            dismissDialog()
        }
    }

    private fun setState(state: State): Boolean {
        val old = _state
        if (old == state) return false

        _state = state
        logMsg(isDebug) { "setState:${state} ${this@FDialog}" }

        if (state.isDismissPart) {
            _showAnimatorFlag = false
        }
        return true
    }

    private fun notifyShow() {
        if (_state != State.Show) return
        logMsg(isDebug) { "notifyShow ${this@FDialog}" }

        _mainHandler.post {
            _onShowListener?.onShow(this@FDialog)
        }
    }

    private fun notifyDismiss() {
        if (_state != State.Dismiss) return
        logMsg(isDebug) { "notifyDismiss ${this@FDialog}" }

        if (_isCanceled) {
            _isCanceled = false
            _mainHandler.post {
                _onCancelListener?.onCancel(this@FDialog)
            }
        }
        _mainHandler.post {
            _onDismissListener?.onDismiss(this@FDialog)
        }
    }

    private fun setDefaultConfig() {
        logMsg(isDebug) { "set default config ${this@FDialog}" }
        if (animatorFactory == null) {
            when (gravity) {
                Gravity.CENTER -> {
                    animatorFactory = AlphaFactory()
                    _isAnimatorFactoryModifiedInternal = true
                }
                Gravity.LEFT,
                Gravity.LEFT or Gravity.CENTER,
                -> {
                    animatorFactory = SlideRightLeftRParentFactory()
                    _isAnimatorFactoryModifiedInternal = true
                }
                Gravity.TOP,
                Gravity.TOP or Gravity.CENTER,
                -> {
                    animatorFactory = SlideDownUpRParentFactory()
                    _isAnimatorFactoryModifiedInternal = true
                }
                Gravity.RIGHT,
                Gravity.RIGHT or Gravity.CENTER,
                -> {
                    animatorFactory = SlideLeftRightRParentFactory()
                    _isAnimatorFactoryModifiedInternal = true
                }
                Gravity.BOTTOM,
                Gravity.BOTTOM or Gravity.CENTER,
                -> {
                    animatorFactory = SlideUpDownRParentFactory()
                    _isAnimatorFactoryModifiedInternal = true
                }
            }
        }
    }

    private fun resetConfig() {
        logMsg(isDebug) { "reset config ${this@FDialog}" }
        if (_isAnimatorFactoryModifiedInternal) {
            animatorFactory = null
        }
    }

    private val _animatorHandler by lazy {
        FVisibilityAnimatorHandler().apply {
            this.setShowAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    logMsg(isDebug) { "animator show onAnimationStart ${this@FDialog}" }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    logMsg(isDebug) { "animator show onAnimationCancel ${this@FDialog}" }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    logMsg(isDebug) { "animator show onAnimationEnd ${this@FDialog}" }
                }
            })

            this.setHideAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    logMsg(isDebug) { "animator dismiss onAnimationStart ${this@FDialog}" }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    logMsg(isDebug) { "animator dismiss onAnimationCancel ${this@FDialog}" }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    logMsg(isDebug) { "animator dismiss onAnimationEnd ${this@FDialog}" }
                    dismissDialog(true)
                }
            })
        }
    }

    private fun createAnimator(show: Boolean): Animator? {
        if (!_dialogView.isAttachedToWindow) {
            return null
        }

        val animatorBackground = if (isBackgroundDim) {
            _backgroundViewAnimatorFactory.createAnimator(show, backgroundView)
        } else {
            null
        }

        val factory = animatorFactory
        val view = _contentView
        val animatorContent = if (factory == null || view == null) {
            null
        } else {
            factory.createAnimator(show, view)
        }

        val animator = if (animatorContent != null && animatorBackground != null) {
            val duration = getAnimatorDuration(animatorContent)
            if (duration < 0) error("Illegal duration:${duration}")
            animatorBackground.duration = duration

            AnimatorSet().apply {
                this.play(animatorBackground).with(animatorContent)
            }
        } else {
            animatorContent ?: animatorBackground
        }

        if (animatorDuration > 0) {
            animator?.duration = animatorDuration
        }

        logMsg(isDebug) {
            val textIsShow = if (show) "show" else "dismiss"
            val textIsNull = if (animator == null) "null" else "not null"
            "animator $textIsShow create $textIsNull ${this@FDialog}"
        }
        return animator
    }

    private val _backgroundViewAnimatorFactory: AnimatorFactory by lazy {
        object : ObjectAnimatorFactory() {
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

            override fun onAnimationStart(show: Boolean, view: View) {
                super.onAnimationStart(show, view)
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(show: Boolean, view: View) {
                super.onAnimationEnd(show, view)
                if (!show) {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    /**
     * Called when the [contentView] changes.
     */
    protected open fun onContentViewChanged(oldView: View?, newView: View?) {}

    /**
     * Similar to [Activity.onCreate], you should initialize your dialog in this method.
     */
    protected open fun onCreate() {}

    /**
     * Called before the dialog is attached.
     */
    protected open fun onStart() {}

    /**
     * Called after the dialog is detached.
     */
    protected open fun onStop() {}

    /**
     * [View.onTouchEvent]
     */
    protected open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    /**
     * Called when the dialog has detected the user's press of the back key.
     * The default implementation simply cancels the dialog (only if it is cancelable), but you can override this to do whatever you want.
     */
    protected open fun onBackPressed() {
        if (_cancelable) {
            logMsg(isDebug) { "onBackPressed try cancel ${this@FDialog}" }
            cancel()
        }
    }

    private fun showDialog() {
        check(_state == State.TryShow) { "Illegal state $_state" }

        val uuid = if (isDebug) UUID.randomUUID().toString() else ""
        logMsg(isDebug) { "showDialog start $uuid ${this@FDialog}" }

        _activityLifecycleCallbacks.register(true)
        FDialogHolder.addDialog(this@FDialog)

        notifyCreate()
        if (_state.isDismissPart) {
            logMsg(isDebug) { "showDialog canceled state changed to $_state when notify onCreate $uuid ${this@FDialog}" }
            return
        }

        notifyStart()
        if (_state.isDismissPart) {
            logMsg(isDebug) { "showDialog canceled state changed to $_state when notify onStart $uuid ${this@FDialog}" }
            return
        }

        setDefaultConfig()
        val display = display
        display.addView(_dialogView)

        if (_dialogView.parent != null) {
            if (setState(State.Show)) {
                notifyShow()
            }
        } else {
            logMsg(isDebug) { "showDialog canceled $display addView failed $uuid ${this@FDialog}" }
            setState(State.Dismiss)
            notifyStop()
            resetConfig()
        }

        logMsg(isDebug) { "showDialog end $uuid ${this@FDialog}" }
    }

    private fun dismissDialog(isAnimator: Boolean = false) {
        val uuid = if (isDebug) UUID.randomUUID().toString() else ""
        logMsg(isDebug) { "dismissDialog start state:$_state isAnimator:${isAnimator} $uuid ${this@FDialog}" }

        _activityLifecycleCallbacks.register(false)
        FDialogHolder.removeDialog(this@FDialog)

        val isAttachedToWindow = _dialogView.isAttachedToWindow
        if (isAttachedToWindow) {
            display.removeView(_dialogView)
        }

        if (setState(State.Dismiss)) {
            if (isAttachedToWindow) {
                notifyDismiss()
            }
        }

        notifyStop()

        if (_state == State.Dismiss) {
            resetConfig()
        }

        logMsg(isDebug) { "dismissDialog end $uuid ${this@FDialog}" }
    }

    private fun notifyCreate() {
        if (!_isCreated) {
            _isCreated = true
            logMsg(isDebug) { "notify onCreate ${this@FDialog}" }
            onCreate()
        }
    }

    private fun notifyStart() {
        _isStarted = true
        logMsg(isDebug) { "notify onStart ${this@FDialog}" }
        onStart()
    }

    private fun notifyStop() {
        if (_isStarted) {
            _isStarted = false
            logMsg(isDebug) { "notify onStop ${this@FDialog}" }
            onStop()
        }
    }

    private inner class InternalDialogView(context: Context) : FrameLayout(context) {
        val backgroundView: View = View(context)
        val containerView: LinearLayout = InternalContainerView(context)

        private var _isCover = false

        init {
            addView(
                backgroundView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            addView(
                containerView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

        fun notifyCover() {
            if (!_isCover) {
                logMsg(isDebug) { "notifyCover ${this@FDialog}" }
                _isCover = true
                checkFocus(false)
            }
        }

        fun notifyCoverRemove() {
            if (_isCover) {
                logMsg(isDebug) { "notifyCoverRemove ${this@FDialog}" }
                _isCover = false
                checkFocus(true)
            }
        }

        private fun checkFocus(check: Boolean) {
            removeCallbacks(_checkFocusRunnable)
            if (check) {
                post(_checkFocusRunnable)
            }
        }

        private val _checkFocusRunnable = object : Runnable {
            override fun run() {
                if (!this@InternalDialogView.isAttachedToWindow) return
                if (isShowing && FDialogHolder.getLast(_context) == this@FDialog) {
                    if (!hasFocus()) {
                        logMsg(isDebug) { "requestFocus ${this@FDialog}" }
                        requestChildFocus(containerView, containerView)
                    }
                }
                postDelayed(this, 1000L)
            }
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (super.dispatchKeyEvent(event)) return true
            return event.dispatch(_keyEventCallback, keyDispatcherState, this)
        }

        private val _keyEventCallback = object : KeyEvent.Callback {
            override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    event.startTracking()
                    return true
                }
                return false
            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
                if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)
                    && !event.isCanceled
                    && event.isTracking
                ) {
                    onBackPressed()
                    return true
                }
                return false
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
                return false
            }

            override fun onKeyMultiple(keyCode: Int, count: Int, event: KeyEvent?): Boolean {
                return false
            }
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return if (_state.isDismissPart) {
                true
            } else {
                super.onInterceptTouchEvent(ev)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (_state.isDismissPart) {
                return true
            }

            if (event.action == MotionEvent.ACTION_DOWN) {
                val isTouchOutside = !isViewUnder(_contentView, event.x.toInt(), event.y.toInt())
                if (isTouchOutside) {
                    if (_cancelable && _canceledOnTouchOutside) {
                        logMsg(isDebug) { "touch outside try cancel ${this@FDialog}" }
                        cancel()
                        return true
                    }
                }
            }

            if (this@FDialog.onTouchEvent(event)) {
                return true
            }

            super.onTouchEvent(event)
            return true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            logMsg(isDebug) { "dialog onAttachedToWindow ${this@FDialog}" }
            checkFocus(true)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            logMsg(isDebug) { "dialog onDetachedFromWindow ${this@FDialog}" }
            checkFocus(false)
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== backgroundView && child !== containerView) {
                error("Can not add $child to dialog view.")
            }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === backgroundView || child === containerView) {
                error("Can not remove child from dialog view.")
            }
        }
    }

    private inner class InternalContainerView(context: Context) : LinearLayout(context) {
        override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
            val finalLeft = if (left < 0) paddingLeft else left
            val finalTop = if (top < 0) paddingTop else top
            val finalRight = if (right < 0) paddingRight else right
            val finalBottom = if (bottom < 0) paddingBottom else bottom

            if (finalLeft != paddingLeft
                || finalTop != paddingTop
                || finalRight != paddingRight
                || finalBottom != paddingBottom
            ) {
                super.setPadding(finalLeft, finalTop, finalRight, finalBottom)
            }
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== _contentView) {
                error("Can not add view to container.")
            }
            logMsg(isDebug) { "onContentViewAdded:${child} ${this@FDialog}" }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === _contentView) {
                // The content view is directly removed from the outside.
                dismiss()
            }
            logMsg(isDebug) { "onContentViewRemoved:${child} ${this@FDialog}" }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            startShowAnimator()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            logMsg(isDebug) { "container onAttachedToWindow state:$_state ${this@FDialog}" }
            if (_state.isShowPart) {
                _showAnimatorFlag = true
                startShowAnimator()
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            logMsg(isDebug) { "container onDetachedFromWindow state:$_state ${this@FDialog}" }
        }

        private fun startShowAnimator() {
            if (_showAnimatorFlag) {
                val width = containerView.width
                val height = containerView.height
                if (width > 0 && height > 0) {
                    logMsg(isDebug) { "startShowAnimator width:${width} height:${height} ${this@FDialog}" }
                    _showAnimatorFlag = false
                    _animatorHandler.setShowAnimator(createAnimator(true))
                    _animatorHandler.startShowAnimator()
                }
            }
        }
    }

    internal fun notifyCover() {
        _dialogView.notifyCover()
    }

    internal fun notifyCoverRemove() {
        _dialogView.notifyCoverRemove()
    }

    private val _activityLifecycleCallbacks by lazy {
        DialogActivityLifecycleCallbacks()
    }

    private inner class DialogActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        fun register(register: Boolean) {
            if (_context !is Activity) return
            val application = _context.applicationContext as Application
            application.unregisterActivityLifecycleCallbacks(this)
            if (register) {
                application.registerActivityLifecycleCallbacks(this)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (activity === _context) {
                logMsg(isDebug) { "onActivityDestroyed ${this@FDialog}" }
                FDialogHolder.remove(activity)
                dismiss()
            }
        }
    }

    init {
        gravity = Gravity.CENTER
        isBackgroundDim = true

        val defaultPadding = (context.resources.displayMetrics.widthPixels * 0.1f).toInt()
        padding.set(defaultPadding, 0, defaultPadding, 0)
    }

    companion object {
        /**
         * Get all dialog of [context].
         */
        @JvmStatic
        fun getAll(context: Context?): List<FDialog>? {
            if (context == null) return null
            return FDialogHolder.get(context)
        }

        /**
         * Close all dialog of [context].
         */
        @JvmStatic
        fun dismissAll(context: Context?) {
            if (context == null) return
            if (context.isFinishing()) return

            val list = getAll(context) ?: return
            for (item in list) {
                item.dismiss()
            }
        }
    }
}

private enum class State {
    TryShow,
    Show,

    TryDismiss,
    Dismiss;

    val isShowPart: Boolean
        get() = this == Show || this == TryShow

    val isDismissPart: Boolean
        get() = this == Dismiss || this == TryDismiss
}

private fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
    return if (view == null) {
        false
    } else {
        x >= view.left && x < view.right && y >= view.top && y < view.bottom
    }
}

private fun getAnimatorDuration(animator: Animator): Long {
    var duration = animator.duration
    if (duration > 0) return duration

    if (animator is AnimatorSet) {
        for (item in animator.childAnimations) {
            val itemDuration = getAnimatorDuration(item)
            if (itemDuration > duration) {
                duration = itemDuration
            }
        }
    }
    return duration
}

private object FDialogHolder {
    private val dialogHolder: MutableMap<Context, MutableList<FDialog>> = hashMapOf()

    fun addDialog(dialog: FDialog) {
        val context = dialog.context
        val holder = dialogHolder[context] ?: mutableListOf<FDialog>().also {
            dialogHolder[context] = it
        }

        if (!holder.contains(dialog)) {
            holder.lastOrNull()?.notifyCover()
            holder.add(dialog)
        }
    }

    fun removeDialog(dialog: FDialog) {
        val context = dialog.context
        val holder = dialogHolder[context] ?: return

        val remove = holder.remove(dialog)
        if (remove) {
            holder.lastOrNull()?.notifyCoverRemove()
            if (holder.isEmpty()) {
                dialogHolder.remove(context)
            }
        }
    }

    fun get(context: Context): List<FDialog>? {
        return dialogHolder[context]?.toList()
    }

    fun getLast(context: Context): FDialog? {
        return dialogHolder[context]?.lastOrNull()
    }

    fun remove(context: Context) {
        dialogHolder.remove(context)
    }
}

private fun findContext(input: Context): Context {
    var context: Context? = input
    while (true) {
        if (context == null) break
        if (context is Activity) break
        if (context is Application) break
        if (context is ContextWrapper) {
            context = context.baseContext
        }
    }
    return context ?: input
}

private fun Context.isFinishing(): Boolean {
    return if (this is Activity) this.isFinishing else false
}

internal inline fun logMsg(isDebug: Boolean, block: () -> String) {
    if (isDebug) {
        Log.i("view-dialog", block())
    }
}