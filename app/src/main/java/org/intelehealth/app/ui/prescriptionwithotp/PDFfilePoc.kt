package org.intelehealth.app.ui.prescriptionwithotp

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PDFfilePoc {

    fun createAndSharePdf(context: Context) {
        // Step 1: Create PDF with unique name and delete if exists
        val fileName = "prescription_${System.currentTimeMillis()}.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val pdfFile = File(directory, fileName)

        // Delete existing file if it exists
        if (pdfFile.exists()) {
            pdfFile.delete()
        }

        // Step 2: Create PDF using Android's PdfDocument
        val pdfDocument = PdfDocument()

        // Create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f

        // Step 2a: Define the HTML content as text (simulating simple HTML parsing)
        val htmlContent = """
        <html>
        <head>
            <style>
                body { font-family: sans-serif; font-size: 14px; }
                .header { text-align: center; font-weight: bold; }
                .section-title { font-weight: bold; text-decoration: underline; }
                .section-content { margin-left: 5px; margin-bottom: 8px; }
                .signature { text-align: right; font-size: 18px; font-family: cursive; }
                .doctor-details { text-align: center; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="header">Arogya Setu<br/>Intelehealth</div>
            <div><b>Patient:</b> Somnath Devrao Bagul</div>
            <div><b>Age/Gender:</b> 45 M</div>
            <div><b>Address:</b> Contact Nashik</div>
            <div><b>Patient ID:</b> 115514 <b>Date of Visit:</b> 16-Jan-2023</div>
            <div class="section-title">Vitals</div>
            <div class="section-content">
                Height: 153 | Weight: 57 | BMI: 24.35<br/>
                Blood Pressure: 120/80 | Respiratory Rate: 18<br/>
                Temperature: 97.5°F | SPO2: 97
            </div>
            <div class="section-title">Diagnostics</div>
            <div class="section-content">
                Glucose (Random): 0 | Glucose (Fasting): 0 |<br/>
                Haemoglobin: 0 | Total Cholesterol: 0
            </div>
            <div class="section-title">Presenting Complaint(s)</div>
            <div class="section-content">
                • Shoulder, arm or hand pain
            </div>
            <div class="section-title">Diagnosis</div>
            <div class="section-content">
                • Musculoskeletal disease Primary & Provisional
            </div>
            <div class="section-title">Medication Plan</div>
            <div class="section-content">
                • Collection Sodium Tablets 50mg 50mg<br/>
                1 Tablet Twice daily after meals<br/>
                Regimen for 5 days
            </div>
            <div class="section-title">General Advice</div>
            <div class="section-content">
                • Do local massage
            </div>
            <div class="section-title">Follow Up Date</div>
            <div class="section-content">
                21-01-2023
            </div>
            <div class="signature">Kajal</div>
            <div class="doctor-details">
                Dr. Kajal Thakur<br/>
                General Physician
            </div>
        </body>
        </html>
    """.trimIndent()

        // Step 2b: Manually parse and format HTML content
        val lines = htmlContent.split("\n")
        var yPosition = 50f

        // Start parsing the content
        for (line in lines) {
            // Detect and handle HTML tags manually for basic formatting
            if (line.contains("<b>") && line.contains("</b>")) {
                paint.isFakeBoldText = true // Bold text
            } else {
                paint.isFakeBoldText = false
            }

            if (line.contains("<i>") && line.contains("</i>")) {
                paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.ITALIC) // Italic text
            } else {
                paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            }

            if (line.contains("<u>") && line.contains("</u>")) {
                paint.flags = Paint.UNDERLINE_TEXT_FLAG // Underlined text
            } else {
                paint.flags = 0
            }

            // Remove HTML tags before rendering text
            val cleanLine = line.replace(Regex("<.*?>"), "")

            // Draw the formatted line on the PDF
            canvas.drawText(cleanLine, 50f, yPosition, paint)
            yPosition += 20f // Adjust spacing between lines
        }

        // Finish the page and document
        pdfDocument.finishPage(page)

        // Step 3: Write the document to the file
        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            // Step 4: Share PDF to WhatsApp
            sharePdfToWhatsApp(context, pdfFile)
        } catch (e: IOException) {
            Log.e("PDF Creation", "Error creating PDF", e)
        }
    }

    fun sharePdfToWhatsApp(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.whatsapp")

            // Grant permissions for the file URI
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Share PDF directly to WhatsApp without showing PDF viewer
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("WhatsApp Share", "Error sharing PDF", e)
        }
    }

    /*   fun createAndSharePdf(context: Context) {
           // Step 1: Create PDF with unique name and delete if exists
           val fileName = "prescription_${System.currentTimeMillis()}.pdf"
           val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
           val pdfFile = File(directory, fileName)

           // Delete the existing file if it exists
           if (pdfFile.exists()) {
               pdfFile.delete()
           }

           // Step 2: Create PDF using Android's PdfDocument
           val pdfDocument = PdfDocument()

           // Create a page description
           val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
           val page = pdfDocument.startPage(pageInfo)

           val canvas: Canvas = page.canvas
           val paint = Paint()

           // Step 2a: Write content to PDF (two lines of text)
           paint.color = Color.BLACK
           paint.textSize = 16f
           canvas.drawText("Prescription", 50f, 50f, paint)
           canvas.drawText("This is the first line of the prescription.", 50f, 100f, paint)
           canvas.drawText("This is the second line of the prescription.", 50f, 150f, paint)

           // Step 2b: Finish the page and the document
           pdfDocument.finishPage(page)

           // Step 3: Write the document to the file
           try {
               pdfDocument.writeTo(FileOutputStream(pdfFile))
               pdfDocument.close()

               // Step 4: Share PDF to WhatsApp
               sharePdfToWhatsApp(context, pdfFile)
           } catch (e: IOException) {
               Log.e("PDF Creation", "Error creating PDF", e)
           }
       }

       fun sharePdfToWhatsApp(context: Context, pdfFile: File) {
           try {
               val uri: Uri = FileProvider.getUriForFile(
                   context,
                   "${context.packageName}.fileprovider",
                   pdfFile
               )
               val intent = Intent(Intent.ACTION_SEND)
               intent.type = "application/pdf"
               intent.putExtra(Intent.EXTRA_STREAM, uri)
               intent.setPackage("com.whatsapp")

               // Grant permissions for the file URI
               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

               // Step 4: Share PDF directly to WhatsApp without showing PDF viewer
               context.startActivity(intent)
           } catch (e: Exception) {
               Log.e("WhatsApp Share", "Error sharing PDF", e)
           }
       }*/

    // Generate PDF asynchronously using WebView and callback once PDF is generated
    fun generatePdfFromHtmlAsync(context: Context, htmlContent: String, callback: (File?) -> Unit) {
        Log.d("PDF", "Starting PDF generation...")

        val pdfFile = File(context.cacheDir, "prescription.pdf")

        // Create a WebView to render the HTML content and then convert it to PDF
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("PDF", "WebView finished loading, starting PDF generation...")

                try {
                    // Create the PDF document
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    val canvas: Canvas = page.canvas
                    // Render the content of the WebView into the canvas of the PDF
                    webView.draw(canvas)

                    pdfDocument.finishPage(page)

                    // Write PDF to file
                    pdfDocument.writeTo(FileOutputStream(pdfFile))
                    Log.d("PDF", "PDF created successfully at ${pdfFile.absolutePath}")
                    callback(pdfFile)  // Callback to notify when PDF is ready

                    pdfDocument.close()

                } catch (e: Exception) {
                    Log.e("PDF", "Error during PDF generation: ${e.message}")
                    callback(null)  // Notify failure by passing null
                }
            }
        }

        // Ensure WebView has enough time to load the content
        webView.webChromeClient = WebChromeClient()
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

        Log.d("PDF", "WebView loading started")
    }

}