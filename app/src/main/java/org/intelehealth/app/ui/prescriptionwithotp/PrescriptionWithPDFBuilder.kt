package org.intelehealth.app.ui.prescriptionwithotp

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.databinding.LayoutPrescriptionPdfBinding
import org.intelehealth.app.models.ClsDoctorDetails
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import android.util.Base64

class PrescriptionWithPDFBuilder(
    private val context: Activity,
    private var patientDataSections: Map<String, Map<String, String?>>
) {
    private val binding: LayoutPrescriptionPdfBinding = LayoutPrescriptionPdfBinding.inflate(LayoutInflater.from(context))
    var generatedFile: File? = null
    var simpleGeneratedFile: File? = null

    fun buildDynamicUI() {
        val container = binding.dynamicContainer ?: return
        container.removeAllViews()

        for ((sectionTitle, sectionData) in patientDataSections) {
            val showLabels = sectionTitle.equals(PrescriptionDetailsDataKeys.VITALS_SECTION, ignoreCase = true) ||
                    sectionTitle.equals(PrescriptionDetailsDataKeys.DIAGNOSTICS_SECTION, ignoreCase = true)
            val groupedContents = sectionData.entries.chunked(3).map { group ->
                group.joinToString(" | ") { (key, value) ->
                    val displayValue = checkValueAndReturnNA(value)
                    if (showLabels) {
                        val label = PrescriptionDetailsDataKeys.getLabelForKey(key)?.takeIf { it.isNotBlank() }
                        if (label != null) "$label: $displayValue" else displayValue
                    } else {
                        displayValue
                    }
                }
            }
            addSection(container, sectionTitle, groupedContents)
        }

    }

    private fun addSection(container: LinearLayout, title: String, contents: List<String>) {
        val underlinedTitle = SpannableString(title).apply {
            setSpan(UnderlineSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val titleView = TextView(context).apply {
            text = underlinedTitle
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 4)
            textSize = 22f
        }

        val contentView = TextView(context).apply {
            text = contents.joinToString("\n").removePrefix("\n")
            setPadding(0, 0, 0, 8)
            textSize = 20f
        }

        container.addView(titleView)
        container.addView(contentView)
    }

    fun build(fileName: String) {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(metrics)
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
        }

        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        binding.root.layout(0, 0, metrics.widthPixels, binding.root.measuredHeight)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            metrics.widthPixels, binding.root.measuredHeight, 1
        ).create()

        val page = pdfDocument.startPage(pageInfo)
        binding.root.draw(page.canvas)
        pdfDocument.finishPage(page)

        //  Safely build file path without duplicating full paths
        val filePath = File(fileName)
        val downloadsDir = filePath.parentFile ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val finalFile = if (filePath.isAbsolute) filePath else File(downloadsDir, fileName)

        try {
            //Overwrite the file if it exists
            FileOutputStream(finalFile, false).use { fos ->
                pdfDocument.writeTo(fos)
            }
            generatedFile = finalFile
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error saving PDF", e)
        } finally {
            pdfDocument.close()
        }

        createSimpleImagePdf(finalFile, File(downloadsDir, getSimpleFileName(finalFile.name)))
    }

    private fun createSimpleImagePdf(sourcePdf: File, outputPdf: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(sourcePdf, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val pdfDocument = PdfDocument()

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)

                // Render original page as bitmap
                var bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Scale bitmap to a target width (example: 1080px or printer width)
                val targetWidth = 1080
                val scaleFactor = targetWidth.toFloat() / bitmap.width
                val scaledHeight = (bitmap.height * scaleFactor).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)

                // Draw scaled bitmap into new PDF page
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(pdfPage)

                FileOutputStream(outputPdf).use { fos ->
                    pdfDocument.writeTo(fos)
                }

                simpleGeneratedFile = outputPdf
            }

            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error generating simple PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSimpleFileName(originalFileName: String): String {
        val baseName = originalFileName.substringBeforeLast(".pdf")
        return "${baseName}.pdf"
    }

    private fun checkValueAndReturnNA(value: String?): String {
        return if (value.isNullOrBlank()) "NA" else value
    }

    fun setPatientDataSections(data: Map<String, Map<String, String?>>) {
        patientDataSections = data
    }

    fun setPatientData(patientData: String) {
        binding.tvPatientDetails.text = patientData
    }

    fun createSignatureBitmap(drDetails: ClsDoctorDetails) {
      /*  val drSignTextView = binding.drSignTextview
       val font =  getFontFamily(fontFamily)
        val typeface = try {
            Typeface.createFromAsset(context.assets, font)
        } catch (e: Exception) {
            e.printStackTrace()
            Typeface.DEFAULT
        }

        drSignTextView.typeface = typeface
        drSignTextView.textSize = 60f
        drSignTextView.setIncludeFontPadding(false)
        drSignTextView.setTextColor(ContextCompat.getColor(context, R.color.ink_pen))
        drSignTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        drSignTextView.text = drSignText
        drSignTextView.isDrawingCacheEnabled = true

        drSignTextView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        drSignTextView.layout(0, 0, drSignTextView.measuredWidth, drSignTextView.measuredHeight)
        */
        decodeBase64ToBitmap(drDetails.signature)?.let { bitmap ->
            binding.imageviewDrSign.setImageBitmap(bitmap)
        } ?: run {
            binding.imageviewDrSign.setImageDrawable(null)
        }

        val drDetailsVal = "${drDetails.name}\n${drDetails.qualification}, ${drDetails.specialization}\n${drDetails.registrationNumber}"
        binding.drDetailsTextview.text = drDetailsVal
    }
    private fun getFontFamily(fontFamily: String): String{
        val fontFamilyFile = when (fontFamily.lowercase()) {
            "youthness" -> "fonts/Youthness.ttf"
            "asem" -> "fonts/Asem.otf"
            "arty" -> "fonts/Arty.otf"
            "almondita" -> "fonts/Almondita.ttf"
            else -> ""
        }
        return fontFamilyFile

    }
    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val base64Cleaned = if (base64String.contains("base64,")) {
                base64String.substring(base64String.indexOf("base64,") + 7)
            } else {
                base64String
            }

            val decodedBytes = Base64.decode(base64Cleaned, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
