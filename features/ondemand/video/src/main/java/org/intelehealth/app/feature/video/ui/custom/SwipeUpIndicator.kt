package org.intelehealth.app.feature.video.ui.custom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView


/**
 * Created by Vaghela Mithun R. on 18-12-2023 - 17:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SwipeUpIndicator : AppCompatImageView {
    private var animator: ObjectAnimator = ObjectAnimator()

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        startAnim()
    }

    private fun startAnim() {
        animator = ObjectAnimator.ofFloat(this, "translationY", 0f, 50f)
        animator.interpolator = AccelerateInterpolator()
        animator.startDelay = 0
        animator.duration = 1000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            alpha = it.animatedFraction
        }
        animator.start()
    }

    fun start() = animator.start()
    fun pause() = animator.pause()
}