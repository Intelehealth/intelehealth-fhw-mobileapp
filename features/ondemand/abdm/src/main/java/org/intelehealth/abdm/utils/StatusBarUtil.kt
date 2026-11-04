package org.intelehealth.abdm.utils

import android.graphics.Color
import android.view.View
import android.view.Window
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object StatusBarUtil {

    @JvmStatic
    @Suppress("DEPRECATION")
    fun setStatusBarChanges(window: Window, rootView: View) {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightNavigationBars = true
        controller.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView,
            OnApplyWindowInsetsListener { view: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )
                view!!.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                WindowInsetsCompat.CONSUMED
            })
    }
}