package org.intelehealth.klivekit.call.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.github.ajalt.timberkt.Timber
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Math.min

/**
 * Created by Vaghela Mithun R. on 18-12-2023 - 17:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FabSwipeable : FloatingActionButton, OnTouchListener {
    private var viewY = 0f

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
        setOnTouchListener(this)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        event?.let {
            if (viewY == 0f) viewY = y
            val newY = viewY.coerceAtMost(it.rawY)
            Timber.tag(TAG).d("Y=>$y")
            Timber.tag(TAG).d("RawY=>${it.rawY}")
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    this.animate().y(newY)
                        .setDuration(0)
                        .alpha(newY / viewY)
                        .start()
                    Timber.tag(TAG).d("newY=>$newY")
                }

                MotionEvent.ACTION_UP -> {}
            }
        }
        return true
    }

    companion object {
        const val TAG = "FabSwipeable"
    }
}