package org.intelehealth.app.activities.notification.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New
import org.intelehealth.app.activities.notification.NotificationList
import org.intelehealth.app.activities.notification.listeners.ClearNotificationListener
import org.intelehealth.app.activities.notification.listeners.CloudNotificationClickListener
import org.intelehealth.app.activities.notification.listeners.NotificationClickListener
import org.intelehealth.app.activities.notification.result.NotificationResult
import org.intelehealth.app.activities.notification.viewmodel.NotificationViewModel
import org.intelehealth.app.activities.visit.PrescriptionActivity
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.databinding.ActivityNotificationBinding
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.Logger
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.klivekit.data.PreferenceHelper


/**
 *
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
private const val TAG = "@@NotificationActivity::"

class NotificationActivity : BaseActivity(), ClearNotificationListener {
    private var notificationAdapter: NotificationAdapter? = null
    private var cloudNotificationAdapter: NotificationCloudAdapter? = null
    private var notificationList: ArrayList<NotificationModel>? = null
    private var notificationCloudList: ArrayList<NotificationList>? = null
    private lateinit var mBinding: ActivityNotificationBinding
    private lateinit var mViewModel: NotificationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        mViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        PreferenceHelper(this).save(PreferenceHelper.IS_NOTIFICATION, false)

        initialization()
        setListeners()

        mBinding.simpleAppBar.toolbar.title = getString(R.string.notifi_title)
        setSupportActionBar(mBinding.simpleAppBar.toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            mBinding.simpleAppBar.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }

    }

    private fun initialization() {

        mViewModel.fetchNonDeletedNotification().observe(this) {
            when (it) {
                is NotificationResult.Loading -> {
                    mBinding.progressBar.visibility = VISIBLE
                    mBinding.rlPrescriptionHeader.visibility = GONE
                    mBinding.ibClearAll.visibility = GONE

                }

                is NotificationResult.Data -> {

                    notificationList = it.data as ArrayList<NotificationModel>
                    setNotificationAdapter()
                    if (!notificationList.isNullOrEmpty()) {
                        mBinding.notifiHeaderTitle.text = String.format(
                            getString(
                                R.string.five_presc_received,
                                mViewModel.getPrescriptionCount().toString()
                            )
                        )

                    }
                }

                else -> {}
            }
        }


        mViewModel.fetchAllCloudNotification(sessionManager.providerID,"1","100").observe(this) {
            when (it) {
                is NotificationResult.Loading -> {
                    mBinding.progressBar.visibility = VISIBLE
                    mBinding.rlPrescriptionHeader.visibility = GONE
                    mBinding.ibClearAll.visibility = GONE
                }

                is NotificationResult.Data -> {
                    Logger.logD("TAG",it.data.toString())
                    mBinding.progressBar.visibility = GONE
                    mBinding.rlPrescriptionHeader.visibility = VISIBLE
                    mBinding.ibClearAll.visibility = VISIBLE
                    notificationCloudList = it.data as ArrayList<NotificationList>
                    setCloudNotificationAdapter()

                }

                else -> {
                    Logger.logD("TAG",it.toString())
                }
            }
        }

    }
    fun showAlertDialog(message :String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Notification")
        builder.setMessage(message)

        // Add the OK button
        builder.setPositiveButton("OK") { dialog, _ ->
            // User clicked OK button
            dialog.dismiss()
        }

        // Create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun setListeners() {
        mBinding.apply {
//            ivBack.setOnClickListener {
//                onBackPressedDispatcher.onBackPressed()
//            }
            ibClearAll.setOnClickListener {
                DeleteNotificationDialog.newInstance(
                    supportFragmentManager,
                    this@NotificationActivity
                )
            }
        }
    }

    private fun setNotificationAdapter() {
        mBinding.tvNoData.visibility = GONE
        mBinding.ibClearAll.visibility = VISIBLE
        mBinding.rvNotifications.apply {
            notificationAdapter = NotificationAdapter(notificationList, clickListener)
            adapter = notificationAdapter
            layoutManager =
                LinearLayoutManager(this@NotificationActivity, RecyclerView.VERTICAL, false)
        }
    }
    private fun setCloudNotificationAdapter() {
        if (notificationList.isNullOrEmpty() && notificationCloudList.isNullOrEmpty()) {
            clearNotification()
        } else {
            mBinding.tvNoData.visibility = GONE
            mBinding.ibClearAll.visibility = VISIBLE
            mBinding.rvCloudNotifications.apply {
                cloudNotificationAdapter = NotificationCloudAdapter(notificationCloudList, clickListener)
                adapter = cloudNotificationAdapter
                layoutManager =
                    LinearLayoutManager(this@NotificationActivity, RecyclerView.VERTICAL, false)
            }
        }
    }

    private val clickListener = object : NotificationClickListener, CloudNotificationClickListener {
        override fun deleteNotification(notificationModel: NotificationModel, position: Int) {
            mViewModel.deleteNotification(notificationModel.uuid)
            notificationList?.removeAt(position)
            notificationAdapter?.notifyItemRemoved(position)
        }

        override fun openNotification(notificationModel: NotificationModel, position: Int) {
            notificationModel.apply {
                if (notificationModel.notification_type == NotificationDbConstants.FOLLOW_UP_NOTIFICATION) {
                    visitUUID = uuid.split(" ")[0]
                }
                if (visitUUID.isNullOrBlank()) {
                    ToastUtil.showLongToast(
                        this@NotificationActivity,
                        getString(R.string.this_visit_is_completed)
                    )
                } else {
                    if (notificationModel.notification_type == NotificationDbConstants.FOLLOW_UP_NOTIFICATION) {
                        val intent = Intent(
                            this@NotificationActivity,
                            FollowUpPatientActivity_New::class.java
                        ).apply {
                            putExtra("uuid", notificationModel.uuid)
                        }
                        startActivity(intent)
                    } else {
                        val intent =
                            Intent(this@NotificationActivity, PrescriptionActivity::class.java)
                        intent.putExtra("patientname", "$first_name $last_name")
                        intent.putExtra("patientUuid", patientuuid)
                        intent.putExtra("patient_photo", patient_photo)
                        intent.putExtra("visit_ID", visitUUID)
                        intent.putExtra("visit_startDate", visit_startDate)
                        intent.putExtra("gender", gender)
                        val vitalsUUID =
                            EncounterDAO.fetchEncounterUuidForEncounterVitals(visitUUID)
                        val adultInitialUUID =
                            EncounterDAO.fetchEncounterUuidForEncounterAdultInitials(visitUUID)
                        intent.putExtra("encounterUuidVitals", vitalsUUID)
                        intent.putExtra("encounterUuidAdultIntial", adultInitialUUID)
                        intent.putExtra(
                            "age",
                            DateAndTimeUtils.getAge_FollowUp(
                                date_of_birth,
                                this@NotificationActivity
                            )
                        )
                        intent.putExtra("tag", "VisitDetailsActivity")
                        intent.putExtra("followupDate", followupDate)
                        intent.putExtra("openmrsID", openmrsID)
                        startActivity(intent)
                    }

                }

            }
        }

        override fun deleteNotification(notificationModel: NotificationList, position: Int) {
            ToastUtil.showLongToast(this@NotificationActivity,"Work in progress..")
        }

        override fun updateReadStatus(notificationModel: NotificationList, position: Int) {
            mViewModel.updateNotificationStatus(notificationModel.id.toString())
            notificationModel.isRead = NotificationDbConstants.READ_STATUS
            notificationModel.title?.let { showAlertDialog(it) }
            cloudNotificationAdapter?.notifyItemChanged(position)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun clearAllNotification() {
        mViewModel.deleteAllNotifications()
        notificationList?.clear()

        mViewModel.clearAllCloudNotification(sessionManager.providerID).observe(this) {
            when (it) {
                is NotificationResult.Loading -> {
                }

                is NotificationResult.Data -> {
                    notificationCloudList?.clear()
                    cloudNotificationAdapter?.notifyDataSetChanged()
                    clearNotification()
                }

                else -> {}
            }
        }

        clearNotification()
        notificationAdapter?.notifyDataSetChanged()
    }

    private fun clearNotification() {
        mBinding.rlPrescriptionHeader.visibility = GONE
        mBinding.tvNoData.visibility = VISIBLE
        mBinding.ibClearAll.visibility = GONE


    }


}