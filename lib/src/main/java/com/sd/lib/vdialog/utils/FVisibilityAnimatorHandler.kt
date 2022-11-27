package com.sd.lib.vdialog.utils

import android.animation.Animator

internal class FVisibilityAnimatorHandler {
    private val _showHandler: IAnimatorHandler = object : AnimatorHandler() {
        override fun beforeAnimatorStart() {
            _hideHandler.cancel()
        }
    }

    private val _hideHandler: IAnimatorHandler = object : AnimatorHandler() {
        override fun beforeAnimatorStart() {
            _showHandler.cancel()
        }
    }

    //---------- Show ----------

    val isShowAnimatorStarted: Boolean get() = _showHandler.isStarted

    fun setShowAnimator(animator: Animator?) {
        _showHandler.setAnimator(animator)
    }

    fun setShowAnimatorListener(listener: Animator.AnimatorListener?) {
        _showHandler.setListener(listener)
    }

    fun startShowAnimator(): Boolean {
        return _showHandler.start()
    }

    fun cancelShowAnimator() {
        _showHandler.cancel()
    }

    //---------- Hide ----------

    val isHideAnimatorStarted: Boolean get() = _hideHandler.isStarted

    fun setHideAnimator(animator: Animator?) {
        _hideHandler.setAnimator(animator)
    }

    fun setHideAnimatorListener(listener: Animator.AnimatorListener?) {
        _hideHandler.setListener(listener)
    }

    fun startHideAnimator(): Boolean {
        return _hideHandler.start()
    }

    fun cancelHideAnimator() {
        _hideHandler.cancel()
    }
}

private interface IAnimatorHandler {
    val isStarted: Boolean

    fun setAnimator(animator: Animator?)

    fun setListener(listener: Animator.AnimatorListener?)

    fun start(): Boolean

    fun cancel()
}

private abstract class AnimatorHandler : IAnimatorHandler {
    private var _animator: Animator? = null
    private val _listener = AnimatorListenerWrapper()

    override val isStarted: Boolean
        get() = _animator?.isStarted ?: false

    override fun setAnimator(animator: Animator?) {
        val old = _animator
        if (old !== animator) {
            old?.removeListener(_listener)
            _animator = animator
            _animator?.addListener(_listener)
        }
    }

    override fun setListener(listener: Animator.AnimatorListener?) {
        _listener.original = listener
    }

    override fun start(): Boolean {
        val animator = _animator ?: return false
        if (animator.isStarted) return true

        beforeAnimatorStart()
        animator.start()
        return true
    }

    override fun cancel() {
        _animator?.cancel()
    }

    abstract fun beforeAnimatorStart()
}

private class AnimatorListenerWrapper : Animator.AnimatorListener {
    var original: Animator.AnimatorListener? = null

    override fun onAnimationStart(animation: Animator) {
        original?.onAnimationStart(animation)
    }

    override fun onAnimationEnd(animation: Animator) {
        original?.onAnimationEnd(animation)
    }

    override fun onAnimationCancel(animation: Animator) {
        original?.onAnimationCancel(animation)
    }

    override fun onAnimationRepeat(animation: Animator) {
        original?.onAnimationRepeat(animation)
    }
}