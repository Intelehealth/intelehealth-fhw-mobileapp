package org.intelehealth.app.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.util.Log
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
            val width = bitmap.width
            val height =
                if (bitmap.height < 6000) bitmap.height / 3
                else if (bitmap.height in 6001..8000) bitmap.height / 4
                else bitmap.height / 5

            Log.d("HHHHH", "" + bitmap.height)
            val pdfDocument = PdfDocument()

            fun addPage(bp: Bitmap, pageNumber: Int) {
                val bitmapPart = Bitmap.createScaledBitmap(bp, 595, 842, true)
                val pageInfo =
                    PdfDocument.PageInfo.Builder(bitmapPart.width, bitmapPart.height, pageNumber)
                        .create()

                val page = pdfDocument.startPage(pageInfo)

                val canvas: Canvas = page.canvas
                canvas.drawBitmap(bitmapPart, 0f, 0f, null)

                pdfDocument.finishPage(page)
            }

            if (bitmap.height < 6000) {
                val firstPart = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                val secondPart = Bitmap.createBitmap(bitmap, 0, height, width, height)
                val thirdPart = Bitmap.createBitmap(bitmap, 0, height * 2, width, height)

                addPage(firstPart, 1)
                addPage(secondPart, 2)
                addPage(thirdPart, 3)

            }
            else if (bitmap.height in 6001..8000) {
                val firstPart = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                val secondPart = Bitmap.createBitmap(bitmap, 0, height, width, height)
                val thirdPart = Bitmap.createBitmap(bitmap, 0, height * 2, width, height)
                val fourthPart = Bitmap.createBitmap(bitmap, 0, height * 3, width, height)

                addPage(firstPart, 1)
                addPage(secondPart, 2)
                addPage(thirdPart, 3)
                addPage(fourthPart, 4)

            }
            else if (8000 < bitmap.height) {
                val firstPart = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                val secondPart = Bitmap.createBitmap(bitmap, 0, height, width, height)
                val thirdPart = Bitmap.createBitmap(bitmap, 0, height * 2, width, height)
                val fourthPart = Bitmap.createBitmap(bitmap, 0, height * 3, width, height)
                val fifthPart = Bitmap.createBitmap(bitmap, 0, height * 4, width, height)

                addPage(firstPart, 1)
                addPage(secondPart, 2)
                addPage(thirdPart, 3)
                addPage(fourthPart, 4)
                addPage(fifthPart, 5)
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