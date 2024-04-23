package org.intelehealth.app.shared.ui.listener

import android.view.View

/**
 * Created by Vaghela Mithun R. on 07/05/21.
 * vaghela.mithun@gmail.com
 */

/**
 * It may be happen that by mistake user will click instantly more then one time a
 * single action event and app suddenly crash. To overcome this type of issue we can block
 * event for periodic time duration. Here @OnSingleClickListener designed to prevent multiple
 * click at a time.
 */
interface OnSingleClickListener : View.OnClickListener {

    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()
        instantClick(v)
        if (currentTimeMillis >= (previousClickTimeMillis + DELAY_MILLIS)) {
            previousClickTimeMillis = currentTimeMillis
            onSingleClick(v)
        }
    }

    fun onSingleClick(v: View)
    fun instantClick(v:View)

    companion object {
        // Tweak this value as you see fit. In my personal testing this
        // seems to be good, but you may want to try on some different
        // devices and make sure you can't produce any crashes.
        private const val DELAY_MILLIS = 1000L

        private var previousClickTimeMillis = 0L
    }
}