package org.intelehealth.app.ui.listener

import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.github.ajalt.timberkt.Timber

/**
 * Created by Vaghela Mithun R. on 18-06-2024 - 19:50.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class DefaultOnItemSelectedListener: OnItemSelectedListener{
    override fun onNothingSelected(p0: AdapterView<*>?) {
        Timber.d { "onNothingSelected" }
    }
}