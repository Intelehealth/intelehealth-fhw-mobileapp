package org.intelehealth.app.activities.notification.view

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import org.intelehealth.app.R
import org.intelehealth.app.activities.notification.viewmodel.NotificationViewModel
import org.intelehealth.app.databinding.ActivityNotificationBinding
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.SessionManager


/**
 *
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
private const val TAG = "@@NotificationActivity::"

class NotificationActivity : BaseActivity() {
    private val sessionManager: SessionManager? = null
    private val db: SQLiteDatabase? = null
     private var notificationList: List<NotificationModel>? = null
    private lateinit var mBinding: ActivityNotificationBinding
    private lateinit var mViewModel: NotificationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        mViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        initialization()
        setListeners()
        setNotificationAdapter()
    }

    private fun initialization() {
    notificationList = mViewModel.fetchNonDeletedNotification()
    }

    private fun setListeners() {
        mBinding.apply {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setNotificationAdapter() {
        mBinding.rvNotifications.apply {
            adapter = NotificationAdapter(notificationList, clickListener)
            layoutManager = LinearLayoutManager(this@NotificationActivity,RecyclerView.VERTICAL,false)
        }
    }

    private val clickListener = object : NotificationClickListener {
        override fun deleteNotification(notificationModel: NotificationModel, position: Int) {
            TODO("Not yet implemented")
        }

        override fun openNotification(notificationModel: NotificationModel, position: Int) {
            TODO("Not yet implemented")
        }

    }


}