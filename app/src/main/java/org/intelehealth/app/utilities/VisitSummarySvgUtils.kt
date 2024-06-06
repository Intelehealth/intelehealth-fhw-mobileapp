package org.intelehealth.app.utilities

import androidx.core.content.ContextCompat
import org.intelehealth.app.R
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryPdfGenerator
import org.intelehealth.app.app.IntelehealthApplication

/**
 * Created By Tanvir Hasan on 5/30/24 11:24 AM
 * Email: tanvirhasan553@gmail.com
 */
class VisitSummarySvgUtils {
    companion object{
        @JvmStatic
        fun getVitalsSvg(): String {
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg width=\"60\" height=\"60\" viewBox=\"0 0 44 44\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                    "  <circle cx=\"22\" cy=\"22\" r=\"22\" fill=\""+outerCircleAlpha+"\" />\n" +
                    "\n" +
                    "  <path d=\"M24.5,24.069C24.5,21.42 24.5,15.201 24.5,14.75C24.5,13.37 23.381,12.25 22,12.25C20.619,12.25 19.5,13.37 19.5,14.75C19.5,15.721 19.5,21.013 19.5,24.069C18.441,24.841 17.75,26.088 17.75,27.5C17.75,29.847 19.653,31.75 22,31.75C24.347,31.75 26.25,29.847 26.25,27.5C26.25,26.088 25.559,24.841 24.5,24.069Z\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linejoin=\"round\" />\n" +
                    "\n" +
                    "  <circle cx=\"22\" cy=\"27.5\" r=\"2.5\" fill=\""+accentColorHex+"\" />\n" +
                    "\n" +
                    "  <path d=\"M22,26.75V18.75\" fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" />\n" +
                    "</svg>"
        }

        @JvmStatic
        fun getVisitReasonSvg(): String {
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg width=\"60\" height=\"60\" viewBox=\"0 0 44 44\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                    "  <!-- Outer Circle -->\n" +
                    "  <circle cx=\"22\" cy=\"22\" r=\"22\" fill=\""+outerCircleAlpha+"\" /> <!-- Replace with @color/colorAccentLightCard -->\n" +
                    "\n" +
                    "  <!-- Path 1 -->\n" +
                    "  <path d=\"M25.25,14.75H27.75C28.579,14.75 29.25,15.422 29.25,16.25V29.75C29.25,30.579 28.579,31.25 27.75,31.25H16.25C15.422,31.25 14.75,30.579 14.75,29.75V16.25C14.75,15.422 15.422,14.75 16.25,14.75H18.75\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 2 -->\n" +
                    "  <path d=\"M23.75,12.75C23.364,12.75 20.524,12.75 20.25,12.75C19.421,12.75 18.75,13.422 18.75,14.25C18.75,15.078 19.421,15.75 20.25,15.75C20.524,15.75 23.364,15.75 23.75,15.75C24.579,15.75 25.25,15.078 25.25,14.25C25.25,13.422 24.579,12.75 23.75,12.75Z\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 3 -->\n" +
                    "  <path d=\"M17.75,21.25H20.75\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 4 -->\n" +
                    "  <path d=\"M17.75,25.75H20.75\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 5 -->\n" +
                    "  <path d=\"M23.25,21.25L24.25,22.25L26.25,20.25\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 6 -->\n" +
                    "  <path d=\"M23.75,27.25L26.25,24.75\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "\n" +
                    "  <!-- Path 7 -->\n" +
                    "  <path d=\"M26.25,27.25L23.75,24.75\"\n" +
                    "        fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" /> <!-- Replace with @color/colorAccent -->\n" +
                    "</svg>"
        }

        @JvmStatic
        fun getPhysicalExamSvg(): String{
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"60\" height=\"60\" viewBox=\"0 0 44 44\">\n" +
                    "    <path fill=\""+outerCircleAlpha+"\" d=\"M22,22m-22,0a22,22 0,1 1,44 0a22,22 0,1 1,-44 0\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" d=\"M27.651,17.207C29.17,17.207 30.401,15.976 30.401,14.457C30.401,12.938 29.17,11.707 27.651,11.707C26.133,11.707 24.901,12.938 24.901,14.457C24.901,15.976 26.133,17.207 27.651,17.207Z\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" d=\"M32.401,20.824C32.401,20.207 31.901,19.707 31.284,19.707H24.019C23.401,19.707 22.901,20.208 22.901,20.824C22.901,20.824 22.901,26.362 22.901,26.456C22.901,29.08 25.028,31.206 27.651,31.206C30.275,31.206 32.401,29.08 32.401,26.456C32.401,26.362 32.401,20.824 32.401,20.824Z\" />\n" +
                    "    <circle cx=\"13.75\" cy=\"14.75\" r=\"1.25\" fill=\""+accentColorHex+"\" />\n" +
                    "    <circle cx=\"18.75\" cy=\"14.75\" r=\"1.25\" fill=\""+accentColorHex+"\" />\n" +
                    "    <circle cx=\"27.75\" cy=\"22.75\" r=\"1.25\" fill=\""+accentColorHex+"\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M13.75,14.75H13.42C11.664,14.75 10.634,16.727 11.641,18.166L16.25,24.75L20.57,18.104C21.509,16.66 20.473,14.75 18.75,14.75\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M16.25,25V25.5C16.25,28.676 18.824,31.25 22,31.25C25.176,31.25 27.75,28.676 27.75,25.5V23.25\" />\n" +
                    "</svg>"
        }

        @JvmStatic
        fun getMedicalHistory(): String{
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"60\" height=\"60\" viewBox=\"0 0 44 44\">\n" +
                    "    <path fill=\""+outerCircleAlpha+"\" d=\"M22,22m-22,0a22,22 0,1,1,44,0a22,22 0,1,1,-44,0\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M27.75,13.25H16.25C15.422,13.25 14.75,13.922 14.75,14.75V29.25C14.75,30.079 15.422,30.75 16.25,30.75H27.75C28.579,30.75 29.25,30.079 29.25,29.25V14.75C29.25,13.922 28.579,13.25 27.75,13.25Z\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M18.25,24.25H25.75\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M18.25,27.25H23.75\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M22,16.75V21.25\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M24,17.75L20,20.25\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M24,20.25L20,17.75\" />\n" +
                    "</svg>\n"
        }

        @JvmStatic
        fun getAdditionalNote(): String{
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"60\" height=\"60\" viewBox=\"0 0 44 44\">\n" +
                    "    <path fill=\""+outerCircleAlpha+"\" d=\"M22,22m-22,0a22,22 0,1,1,44,0a22,22 0,1,1,-44,0\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M20.95,26.099L18.25,26.75L18.901,24.05L23.277,19.674C23.843,19.109 24.76,19.109 25.326,19.674C25.891,20.24 25.891,21.157 25.326,21.723L20.95,26.099Z\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linejoin=\"round\" d=\"M25.164,14.25H27.75C28.579,14.25 29.25,14.922 29.25,15.75V29.25C29.25,30.079 28.579,30.75 27.75,30.75H16.25C15.422,30.75 14.75,30.079 14.75,29.25V15.75C14.75,14.922 15.422,14.25 16.25,14.25H18.853\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M23.75,12.25C23.199,12.25 20.8,12.25 20.25,12.25C19.421,12.25 18.75,12.922 18.75,13.75C18.75,14.578 19.421,15.25 20.25,15.25C20.8,15.25 23.199,15.25 23.75,15.25C24.579,15.25 25.25,14.578 25.25,13.75C25.25,12.922 24.579,12.25 23.75,12.25Z\" />\n" +
                    "</svg>\n"
        }

        @JvmStatic
        fun getDoctorSpeciality(): String{
            val accentColor = ContextCompat.getColor(IntelehealthApplication.getInstance(), R.color.colorAccent)
            val accentColorHex = VisitSummaryPdfGenerator.intToHex(accentColor)
            val outerCircleAlpha = "${accentColorHex}33"
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"60\" height=\"60\" viewBox=\"0 0 44 44\">\n" +
                    "    <path fill=\""+outerCircleAlpha+"\" d=\"M22,22m-22,0a22,22 0,1,1,44,0a22,22 0,1,1,-44,0\" />\n" +
                    "    <circle cx=\"19.5\" cy=\"24\" r=\"0.5\" fill=\""+accentColorHex+"\" />\n" +
                    "    <circle cx=\"24.5\" cy=\"24\" r=\"0.5\" fill=\""+accentColorHex+"\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M27.75,11.75H16.25C15.698,11.75 15.25,12.198 15.25,12.75V20.25H28.75V12.75C28.75,12.198 28.302,11.75 27.75,11.75Z\" />\n" +
                    "    <rect x=\"21.75\" y=\"14\" width=\"0.5\" height=\"3\" fill=\""+accentColorHex+"\" />\n" +
                    "    <rect x=\"20.25\" y=\"15.5\" width=\"3\" height=\"0.5\" fill=\""+accentColorHex+"\" />\n" +
                    "    <path fill=\"none\" stroke=\""+accentColorHex+"\" stroke-width=\"1.33\" stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M28.75,20V22.44C29.338,22.719 29.75,23.305 29.75,24C29.75,24.966 28.966,25.75 28,25.75C27.99,25.75 27.98,25.747 27.969,25.747C27.179,29.514 24.806,32.25 22,32.25C19.194,32.25 16.821,29.514 16.031,25.747C16.02,25.747 16.01,25.75 16,25.75C15.033,25.75 14.25,24.966 14.25,24C14.25,23.303 14.66,22.711 15.25,22.431V20\" />\n" +
                    "</svg>\n"
        }
    }
}