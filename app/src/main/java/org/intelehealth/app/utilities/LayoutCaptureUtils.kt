package org.intelehealth.app.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ScrollView


/**
 * Created by Tanvir Hasan on 26-05-2024 : 18-22.
 * Email: mhasan@intelehealth.org
 */
class LayoutCaptureUtils {
    companion object{
        @JvmStatic
        fun captureScrollView(scrollView: ScrollView): Bitmap? {
            var height = 0
            for (i in 0 until scrollView.childCount) {
                height += scrollView.getChildAt(i).height
            }
            val bitmap = Bitmap.createBitmap(scrollView.width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            scrollView.draw(canvas)
            return bitmap
        }
    }
}