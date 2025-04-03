package org.intelehealth.app.registry

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


/**
 * Created by Vaghela Mithun R. on 04/03/21.
 * vaghela.mithun@gmail.com
 */

fun Map<String, Boolean>.allGranted(): Boolean {
    if (keys.isEmpty()) return false

    keys.forEach {
        if (get(it) == false) return false
    }

    return true
}

class PermissionRegistry constructor(
    val context: Context,
    registry: ActivityResultRegistry
) {
    //    private val enabled = MutableLiveData(false)
    private var granted = MutableLiveData<Map<String, Boolean>>()
    private val grantedData: LiveData<Map<String, Boolean>> get() = granted

    private val grantedPermissions = HashMap<String, Boolean>()

    private lateinit var permissions: Array<String>

    private val getPermission = registry.register(REGISTRY_KEY, RequestPermission()) { result ->
        grantedPermissions[permissions[0]] = result
        if (!result) showDeclineDialog()
        else {
            granted.postValue(grantedPermissions)
        }
    }

    private val multiPermission = registry.register(REGISTRY_KEY, RequestMultiplePermissions()) {
        grantedPermissions.putAll(it)
        granted.postValue(grantedPermissions)
    }

    fun requestPermission(permission: String): LiveData<Map<String, Boolean>> {
        var flag = false

        if (!checkPermission(permission))
            getPermission.launch(permission)
        else
            flag = true

        permissions = arrayOf(permission)
        grantedPermissions[permissions[0]] = flag
        granted.postValue(grantedPermissions);
        return grantedData
    }

    fun requestPermissions(permissions: Array<String>): LiveData<Map<String, Boolean>> {
        this.permissions = permissions
        val temp = ArrayList<String>()

        permissions.forEach {
            if (!checkPermission(it)) temp.add(it) else grantedPermissions[it] = true
        }

        if (temp.isEmpty()) granted.value = grantedPermissions
        else {
            val newPermissions = temp.toTypedArray()
            multiPermission.launch(newPermissions)
        }

        return grantedData
    }

    private fun checkPermission(permission: String): Boolean {
        val res: Int = context.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    fun removePermissionObserve(owner: LifecycleOwner) {
        grantedData.removeObservers(owner)
        granted = MutableLiveData();
    }

    private fun showDeclineDialog() {
//        AlertDialog.Builder(context).apply {
//            setMessage(R.string.permission_decline_alert)
//            setNeutralButton(R.string.ok) { dialog, which -> dialog!!.dismiss() }
//        }.show()
    }

    companion object {
        private const val REGISTRY_KEY = "permission_registry_key"
    }
}