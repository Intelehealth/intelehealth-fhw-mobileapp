package org.intelehealth.app.appointmentNew.MyAppointmentNew;

import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocalByCommaContact;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointmentNew.AppointmentDetailsActivity;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class PastMyAppointmentsAdapter extends RecyclerView.Adapter<PastMyAppointmentsAdapter.MyViewHolder> {
    private static final String TAG = "TodaysMyAppointmentsAda";
    Context context;
    List<AppointmentInfo> appointmentInfoList;
    String whichAppointments;
    SessionManager sessionManager;

    public PastMyAppointmentsAdapter(Context context, List<AppointmentInfo> appointmentInfoList, String whichAppointments) {
        this.context = context;
        this.appointmentInfoList = appointmentInfoList;
        this.whichAppointments = whichAppointments;
        sessionManager = new SessionManager(context);
    }

    @Override
    public PastMyAppointmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_appointments_ui2_new, parent, false);
        PastMyAppointmentsAdapter.MyViewHolder myViewHolder = new PastMyAppointmentsAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(PastMyAppointmentsAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: appointmentInfoList : " + appointmentInfoList.size());

        try {
            AppointmentInfo appointmentInfoModel = appointmentInfoList.get(position);
            if (appointmentInfoModel.getPatientProfilePhoto() == null || appointmentInfoModel.getPatientProfilePhoto().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(context)) {
                    profilePicDownloaded(appointmentInfoModel, holder);
                }
            }


            setGenderAgeLocalByCommaContact(context, holder.search_gender, appointmentInfoModel.getPatientDob(),
                    appointmentInfoModel.getPatientGender(), sessionManager);

            if (appointmentInfoModel.getPatientProfilePhoto() != null && !appointmentInfoModel.getPatientProfilePhoto().isEmpty()) {
                RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                        .asDrawable().sizeMultiplier(0.3f);
                Glide.with(context)
                        .load(appointmentInfoModel.getPatientProfilePhoto())
                        .thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.ivProfileImage);
            } else {
                holder.ivProfileImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.avatar1));
            }
            if (whichAppointments.equalsIgnoreCase("upcoming")) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                String currentDateTime = dateFormat.format(new Date());
                String slottime = appointmentInfoModel.getSlotDate() + " " + appointmentInfoModel.getSlotTime();

                long diff = 0;
                try {
                    diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();
                    long second = diff / 1000;
                    long minutes = second / 60;
                    Log.v("AppointmentInfo", "Diff minutes - " + minutes);

                    String timeText = "";
                    //check for appointmet but presc not given and visit not completed
                    holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                        timeText = DateAndTimeUtils.convertDateToDdMmYyyyHhMmFormat(appointmentInfoModel.getSlotDate(),appointmentInfoModel.getSlotTime());
                    else if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                        timeText = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()));
                    holder.tvDate.setText(timeText);
                    holder.tvDate.setTextColor(context.getColor(R.color.iconTintGray));
                    holder.tvDate.setCompoundDrawables(null,null,null,null);

                    if(appointmentInfoModel.getStatus().equalsIgnoreCase("completed")){
                        holder.status_tv.setText(ContextCompat.getString(context,R.string.completed));
                        holder.status_tv.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary1));
                        if (appointmentInfoModel.isPrescription_exists()) {
                            holder.cvPrescRx.setVisibility(View.VISIBLE);
                            holder.cvPrescPending.setVisibility(View.GONE);
                        } else {
                            holder.cvPrescPending.setVisibility(View.VISIBLE);
                            holder.cvPrescRx.setVisibility(View.GONE);
                        }
                    }else if(appointmentInfoModel.getStatus().equalsIgnoreCase("cancelled")){
                        holder.status_tv.setText(ContextCompat.getString(context,R.string.cancelled));
                        holder.status_tv.setTextColor(ContextCompat.getColor(context,R.color.red));
                    }else {
                        holder.status_tv.setText(ContextCompat.getString(context,R.string.missed));
                        holder.status_tv.setTextColor(ContextCompat.getColor(context,R.color.red));
                    }


                } catch (ParseException e) {
                    Log.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }

            holder.cardParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  /*    patientname patientUuid gender age openmrsID visit_ID visit_startDate visit_speciality followup_date
                  priority_tag hasPrescription patient_photo chief_complaint */

                    Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                    intent.putExtra("patientname", appointmentInfoModel.getPatientName());
                    intent.putExtra("patientUuid", appointmentInfoModel.getPatientId());
                    intent.putExtra("gender", "");
                    intent.putExtra("dob", appointmentInfoModel.getPatientDob());
                    //String age = DateAndTimeUtils.getAge_FollowUp(appointmentInfoModel.get(), context);
                    intent.putExtra("age", "");
                    intent.putExtra("priority_tag", "");
                    intent.putExtra("hasPrescription", appointmentInfoModel.isPrescription_exists());
                    intent.putExtra("openmrsID", appointmentInfoModel.getOpenMrsId());
                    intent.putExtra("visit_ID", appointmentInfoModel.getVisitUuid());
                    intent.putExtra("visit_startDate", "");
                    intent.putExtra("patient_photo", appointmentInfoModel.getPatientProfilePhoto());
                    intent.putExtra("app_start_date", appointmentInfoModel.getSlotDate());
                    intent.putExtra("app_start_time", appointmentInfoModel.getSlotTime());
                    intent.putExtra("visit_speciality", appointmentInfoModel.getSpeciality());
                    intent.putExtra("appointment_id", appointmentInfoModel.getId());
                    intent.putExtra("app_start_day", appointmentInfoModel.getSlotDay());
                    intent.putExtra("prescription_received_time", DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time(), context));
                    intent.putExtra("status", appointmentInfoModel.getStatus());

                    context.startActivity(intent);

                }
            });


        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: e main : " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return appointmentInfoList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardParent, cvPrescPending, cvPrescRx;
        TextView tvPatientName, tvDate, search_gender,status_tv;
        ImageView ivProfileImage;
        LinearLayout IvPriorityTag;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardParent = itemView.findViewById(R.id.card_todays_appointments1);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name_todays);
            ivProfileImage = itemView.findViewById(R.id.profile_image_todays);
            tvDate = itemView.findViewById(R.id.tv_date_appointment_todays);
            IvPriorityTag = itemView.findViewById(R.id.llPriorityTagTodayAppointmentItem);
            cvPrescPending = itemView.findViewById(R.id.cvPrescPendingTodayAppointment);
            cvPrescRx = itemView.findViewById(R.id.cvPrescRxTodayAppointment);
            search_gender = itemView.findViewById(R.id.search_gender);
            status_tv = itemView.findViewById(R.id.status_tv);
        }
    }

    public void profilePicDownloaded(AppointmentInfo model, MyViewHolder holder) {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, model.getUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + model.getPatientProfilePhoto());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(model.getUuid(),
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                                    .asDrawable().sizeMultiplier(0.3f);
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg")
                                    .thumbnail(requestBuilder)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(holder.ivProfileImage);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg", model.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

}
