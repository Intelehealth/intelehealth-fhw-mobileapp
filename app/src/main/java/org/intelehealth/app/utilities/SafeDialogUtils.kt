package org.intelehealth.app.utilities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by Tanvir Hasan on 06-11-2025 : 16-34.
 * Email: mhasan@intelehealth.org
 *
 * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
 * added to handle crash when activity is finishing or destroyed
 */
object SafeDialogUtil {

    /**
     * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
     * to handle AlertDialog
     */
    @JvmStatic
    fun showDialog(context: Context?, dialog: AlertDialog) {
        try {
            if (context is Activity) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.show()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity is finishing or destroyed, dialog not shown.")
                }
            } else {
                CustomLog.d("SafeDialogUtil", "Context is not an Activity, dialog not shown.")
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to show dialog safely", e)
        }
    }

    /**
     * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
     * to handle AlertDialog
     */
    @JvmStatic
    fun showDialog(context: Context?, dialog: Dialog) {
        try {
            if (context is Activity) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.show()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity is finishing or destroyed, dialog not shown.")
                }
            } else {
                CustomLog.d("SafeDialogUtil", "Context is not an Activity, dialog not shown.")
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to show dialog safely", e)
        }
    }

    @JvmStatic
    fun dismissDialog(context: Context?, dialog: AlertDialog?) {
        try {
            if (context is Activity && dialog != null && dialog.isShowing) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.dismiss()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity finishing/destroyed, dismiss skipped.")
                }
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to dismiss AlertDialog safely", e)
        }
    }


            /**
     * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
     * to handle android.app.AlertDialog
     */

    @JvmStatic
    fun showDialog(context: Context?, dialog: android.app.AlertDialog) {
        try {
            if (context is Activity) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.show()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity is finishing or destroyed, dialog not shown.")
                }
            } else {
                CustomLog.d("SafeDialogUtil", "Context is not an Activity, dialog not shown.")
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to show dialog safely", e)
        }
    }

    @JvmStatic
    fun dismissDialog(context: Context?, dialog: android.app.AlertDialog?) {
        try {
            if (context is Activity && dialog != null && dialog.isShowing) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.dismiss()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity finishing/destroyed, dismiss skipped.")
                }
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to dismiss android.app.AlertDialog safely", e)
        }
    }



    /**
     * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
     * to handle DatePickerDialog
     */
    @JvmStatic
    fun showDialog(context: Context?, dialog: DatePickerDialog) {
        try {
            if (context is Activity) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.show()
                } else {
                   CustomLog.d("SafeDialogUtil", "Activity is finishing or destroyed, dialog not shown.")
                }
            } else {
               CustomLog.d("SafeDialogUtil", "Context is not an Activity, dialog not shown.")
            }
        } catch (e: Exception) {
           CustomLog.d("SafeDialogUtil", "Failed to show dialog safely", e)
        }
    }

    @JvmStatic
    fun dismissDialog(context: Context?, dialog: DatePickerDialog?) {
        try {
            if (context is Activity && dialog != null && dialog.isShowing) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.dismiss()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity finishing/destroyed, dismiss skipped.")
                }
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to dismiss DatePickerDialog safely", e)
        }
    }
    /**
     * this is a safe dialog class to handle WindowManagerGlobal.findViewLocked (AEAT-1982)
     * to handle AlertDialog with return
     * some class or activity expecting dialog to be return
     */
    @JvmStatic
    fun showDialog(context: Context?, dialog: MaterialAlertDialogBuilder): AlertDialog? {
        try {
            if (context is Activity) {
                if (!context.isFinishing && !context.isDestroyed) {
                    return dialog.show()
                } else {
                   CustomLog.d("SafeDialogUtil", "Activity is finishing or destroyed, dialog not shown.")
                }
            } else {
               CustomLog.d("SafeDialogUtil", "Context is not an Activity, dialog not shown.")
                return null
            }
        } catch (e: Exception) {
           CustomLog.d("SafeDialogUtil", "Failed to show dialog safely", e)
            return null
        }
        return null
    }

    @JvmStatic
    fun dismissDialog(context: Context?, dialog: DialogInterface?) {
        try {
            if (context is Activity && dialog != null) {
                if (!context.isFinishing && !context.isDestroyed) {
                    dialog.dismiss()
                } else {
                    CustomLog.d("SafeDialogUtil", "Activity finishing/destroyed, dismiss skipped.")
                }
            }
        } catch (e: Exception) {
            CustomLog.d("SafeDialogUtil", "Failed to dismiss MaterialAlertDialog safely", e)
        }
    }
}