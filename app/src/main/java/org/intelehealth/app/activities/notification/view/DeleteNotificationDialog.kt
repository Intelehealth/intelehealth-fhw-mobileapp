package org.intelehealth.app.activities.notification.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.intelehealth.app.R
import org.intelehealth.app.activities.notification.listeners.ClearNotificationListener
import org.intelehealth.app.databinding.FragmentDeleteNotificationDiaogBinding

class DeleteNotificationDialog : DialogFragment() {
    private lateinit var mBinding: FragmentDeleteNotificationDiaogBinding
    private lateinit var clickListeners: ClearNotificationListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_delete_notification_diaog,
            container,
            false
        )
        setListeners()
        return mBinding.root
    }

    private fun setListeners() {
        mBinding.tvDelete.setOnClickListener {
            clickListeners.clearAllNotification()
            dismiss()
        }
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(fragmentManager: FragmentManager, listeners: ClearNotificationListener) {
            DeleteNotificationDialog().apply {
                show(fragmentManager, DeleteNotificationDialog::class.simpleName)
                clickListeners = listeners
            }
        }
    }
}