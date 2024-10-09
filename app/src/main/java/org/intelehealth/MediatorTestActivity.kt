package org.intelehealth

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.features.ondemand.mediator.listener.VideoCallListener
import org.intelehealth.features.ondemand.mediator.VIDEO_CALL_IMPL_CLASS
import org.intelehealth.features.ondemand.mediator.createInstance

/**
 * Created by Vaghela Mithun R. on 09-10-2024 - 12:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class MediatorTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this)
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        view.gravity = Gravity.CENTER
        val mediator = createInstance<VideoCallListener>(VIDEO_CALL_IMPL_CLASS)
        mediator?.let {
            view.text = mediator.testMethod()
        }
        setContentView(view)
    }
}