package org.intelehealth.klivekit.call.ui.custom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.BounceInterpolator
import com.github.ajalt.timberkt.Timber
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * Created by Vaghela Mithun R. on 18-12-2023 - 17:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FabSwipeable : FloatingActionButton, OnTouchListener {
    private var viewY = 0f
    private var bounceAnimator: ObjectAnimator = ObjectAnimator()

    lateinit var swipeEventListener: SwipeEventListener

    interface SwipeEventListener {
        fun onTap(){}
        fun onReleased(){}
        fun onSwipe(){}
        fun onCompleted(){}
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        startBounceAnim()
    }

    private fun startBounceAnim() {
        bounceAnimator = ObjectAnimator.ofFloat(this, "translationY", -50f, 100f)
        bounceAnimator.interpolator = BounceInterpolator()
        bounceAnimator.startDelay = 500
        bounceAnimator.duration = 2500
        bounceAnimator.repeatCount = ValueAnimator.INFINITE
        bounceAnimator.repeatMode = ValueAnimator.REVERSE
        bounceAnimator.start()
        setOnTouchListener(this@FabSwipeable)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        event?.let {
            if (viewY == 0f) viewY = y
            val newY = viewY.coerceAtMost(it.rawY)
            val swipe = viewY - newY
            Timber.tag(TAG).d("Y=>$y")
            Timber.tag(TAG).d("RawY=>${it.rawY}")
            Timber.tag(TAG).d("swipe=>$swipe")

            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    this.animate().y(newY)
                        .setDuration(0)
                        .alpha(1 - (swipe / MIN_SWIPE_DISTANCE) + 0.2f)
                        .start()
                    if (::swipeEventListener.isInitialized) swipeEventListener.onSwipe()
                    complete(swipe)
                    Timber.tag(TAG).d("newY=>$newY")
                }

                MotionEvent.ACTION_UP -> {
                    bounceAnimator.start()
                    animate().alpha(1f)
                        .y(viewY)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(0).start()
                    if (::swipeEventListener.isInitialized) swipeEventListener.onReleased()
                    complete(swipe)
                }

                MotionEvent.ACTION_DOWN -> {
                    bounceAnimator.pause()
                    this.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(0)
                        .start()
                    if (::swipeEventListener.isInitialized) swipeEventListener.onTap()
                }
            }
        }
        return true
    }

    private fun complete(swiped: Float) {
        if (swiped > MIN_SWIPE_DISTANCE) {
            Timber.tag(TAG).d("reach to lime=>")
            if (::swipeEventListener.isInitialized) swipeEventListener.onCompleted()
        }
    }

    companion object {
        const val TAG = "FabSwipeable"
        const val MIN_SWIPE_DISTANCE = 600f
    }
}