package org.intelehealth.app.ui.prescriptionwithotp

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SharePrescriptionViewModelFactory (private val db: SQLiteDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharePrescriptionViewModel::class.java)) {
            return SharePrescriptionViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}