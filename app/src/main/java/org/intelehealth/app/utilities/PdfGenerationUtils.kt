package org.intelehealth.app.utilities

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by Tanvir Hasan on 26-05-2024 : 18-23.
 * Email: mhasan@intelehealth.org
 */
class PdfGenerationUtils {
    companion object{
        @JvmStatic
        fun generatePDF(bitmap: Bitmap, filePath: String?): PdfDocument {
            val document = PdfDocument()
            val pageInfo = PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)
            //FileOutputStream(filePath).use { fos -> document.writeTo(fos) }
            document.close()
            return document
        }
    }
}