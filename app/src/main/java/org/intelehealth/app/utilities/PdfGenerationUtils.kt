package org.intelehealth.app.utilities

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.util.DisplayMetrics
import android.util.Log
import org.intelehealth.app.R
import java.io.File
import java.io.FileOutputStream


/**
 * Created by Tanvir Hasan on 26-05-2024 : 18-23.
 * Email: mhasan@intelehealth.org
 */
class PdfGenerationUtils {
    companion object {
        val document = PdfDocument()

        @JvmStatic
        fun generatePDF(context:Context,bitmap: Bitmap, fileName: String?): String {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            val page:Double = bitmap.height/(displayMetrics.heightPixels.toDouble() - context.resources.getDimension(R.dimen.mergin_30dp))
            val width = bitmap.width
            val fullHeight = bitmap.height/
                    if(page.toInt() == 1) 2
                    else if(page.toInt() == 0) 1
                    else page.toInt()

            val partialHeight = (bitmap.height - page.toInt() * fullHeight)

            val pdfDocument = PdfDocument()

            fun addPage(bp: Bitmap, pageNumber: Int) {
                val bitmapPart = Bitmap.createScaledBitmap(bp, 480, 1000, true)
                val pageInfo =
                    PdfDocument.PageInfo.Builder(bitmapPart.width, bitmapPart.height, pageNumber)
                        .create()

                val page = pdfDocument.startPage(pageInfo)

                val canvas: Canvas = page.canvas
                canvas.drawBitmap(bitmapPart, 0f, 0f, null)

                pdfDocument.finishPage(page)
            }

            val partList = mutableListOf<Bitmap>()
            for (i in 0..page.toInt()) {
                if(i == page.toInt()){
                    if(partialHeight > context.resources.getDimension(R.dimen.mergin_30dp)){
                        partList.add(Bitmap.createBitmap(bitmap, 0, fullHeight*i, width, partialHeight.toInt()))
                    }
                }else{
                    partList.add(Bitmap.createBitmap(bitmap, 0, fullHeight*i, width, fullHeight))
                }
            }

            for(i in 1..partList.size){
                addPage(partList[i-1], i)
            }

            val directory = context.filesDir
            val file = File(directory, fileName)

           // val file = filePath?.let { File(it) }
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file.absolutePath
        }

    }
}