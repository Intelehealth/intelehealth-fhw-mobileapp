package org.intelehealth.app.adapter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Tanvir Hasan on 26-05-2024 : 18-42.
 * Email: mhasan@intelehealth.org
 */


class PdfPrintAdapter(context: Context, pdfUri: Uri) :
    PrintDocumentAdapter() {
    private val context: Context
    private val pdfUri: Uri

    init {
        this.context = context
        this.pdfUri = pdfUri
    }

    override fun onStart() {
        // Called when the printing starts.
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder("print_output.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange?>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        try {
            FileInputStream(pdfUri.path?.let { File(it) }).use { fis ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer)
                            .also { bytesRead = it } != -1 && !cancellationSignal.isCanceled
                    ) {
                        output.write(buffer, 0, bytesRead)
                    }
                    if (cancellationSignal.isCanceled) {
                        callback.onWriteCancelled()
                    } else {
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    }
                }
            }
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
        }
    }

    override fun onFinish() {
        // Called when the printing finishes.
    }
}
