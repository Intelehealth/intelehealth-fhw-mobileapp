package org.intelehealth.app.ui.prescriptionwithotp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrescriptionHtmlPdfGenerator(private val context: Context) {

    private var webView: WebView? = null

    fun generateAndSharePdf(htmlContent: String, patientId: String) {
        // Generate dynamic file name based on patient ID
        val fileName = generateFileName(patientId)

        // Set up WebView to render HTML content
        webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }

        // WebViewClient to detect page loading completion
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // WebView content loaded, now we can generate the PDF
                view?.post {
                    generatePdfFromWebView(view, fileName)
                }
            }
        }
    }

    // Method to generate PDF from WebView content
    private fun generatePdfFromWebView(view: WebView?, fileName: String) {
        view?.let {
            val width = it.width
            val height = it.height

            if (width > 0 && height > 0) {
                // Create a Bitmap of the WebView content
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                it.draw(canvas)

                // Create a PDF document
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                // Draw the WebView content onto the PDF page
                val pageCanvas = page.canvas
                pageCanvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                // Save the PDF to the file
                val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$fileName.pdf")
                try {
                    val fileOutputStream = FileOutputStream(pdfFile)
                    pdfDocument.writeTo(fileOutputStream)
                    fileOutputStream.close()
                    pdfDocument.close()

                    // Log the file path to confirm the PDF is created
                    Log.d("PDF File Path", pdfFile.absolutePath)

                    // After saving the PDF, trigger sharing
                    sharePdf(pdfFile)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            } else {
                Log.d("WebView", "WebView size still 0. Cannot generate PDF.")
            }
        }
    }

    // Method to generate a dynamic file name using patient ID and current timestamp
    private fun generateFileName(patientId: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date()) // Get the current date and time
        return "prescription_${patientId}_$timestamp"  // Example: "prescription_115514_20230427_140025"
    }

    // Method to share the generated PDF via WhatsApp
    private fun sharePdf(file: File) {
        // Log the file to check if it's being passed correctly
        Log.d("Share PDF", "Sharing file: ${file.absolutePath}")

        // Get URI for the file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", file
        )

        // Log the URI to verify that it is generated correctly
        Log.d("File URI", uri.toString())

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")  // Ensure WhatsApp is installed
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            // If sharing fails, log an error
            Log.e("Share Error", "Error starting WhatsApp share intent: ${e.message}")
            Toast.makeText(context, "Unable to share PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
