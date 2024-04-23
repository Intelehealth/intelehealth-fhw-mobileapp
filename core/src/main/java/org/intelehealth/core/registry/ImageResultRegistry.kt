package org.intelehealth.core.registry

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Created by Vaghela Mithun R. on 11-03-2022.
 * vaghela.mithun@gmail.com
 */
class ImageResultRegistry(
    val context: Context,
    private val registry: ActivityResultRegistry,
    private val permissionRegistry: PermissionRegistry,
    private val lifecycleOwner: LifecycleOwner
) {
    private val captureResult = ActivityResultContracts.TakePicture()
    private val contentResult = ActivityResultContracts.GetContent()
    private val uriData: MutableLiveData<Uri?> = MutableLiveData()
    private val isPictureTaken = MutableLiveData(false)

    interface OnImageOptionSelectListener {
        fun onSelectGallery()
        fun onSelectCamera()
        fun onCancel()
    }

    private val captureRegistry = registry.register(IMAGE_RESULT_KEY, captureResult) {
        Log.d(TAG, "takePictureRegistry -> hasTaken :: $it")
        isPictureTaken.value = it
    }

    private val contentRegistry = registry.register(IMAGE_RESULT_KEY, contentResult) {
        Log.d(TAG, "getContentRegistry -> Uri :: $it")
        uriData.postValue(it)
    }

    fun unregisterCaptureRegistry() {
        captureRegistry.unregister()
    }

    fun unregisterContentRegistry() {
        contentRegistry.unregister()
    }

    fun capturePicture(uri: Uri): LiveData<Boolean> {
        permissionRegistry.requestPermissions(cameraPermissions).observe(lifecycleOwner) {
            if (it[cameraPermission] == true && it[writeStoragePermission] == true) takePicture(uri)
        }

        return isPictureTaken
    }

    private fun takePicture(uri: Uri) {
        captureRegistry.launch(uri)
    }

    fun pickPicture(): LiveData<Uri?> {
        permissionRegistry.requestPermission(readStoragePermission).observe(lifecycleOwner) {
            if (it[readStoragePermission] == true) contentRegistry.launch("image/*")
        }

        return uriData
    }

    companion object {
        private const val TAG = "ImageResultRegistry"
        private const val IMAGE_RESULT_KEY = "imageResult"
        const val readStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        const val writeStoragePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val cameraPermission = Manifest.permission.CAMERA

        val cameraPermissions = arrayOf(
            writeStoragePermission,
            cameraPermission
        )
    }
}