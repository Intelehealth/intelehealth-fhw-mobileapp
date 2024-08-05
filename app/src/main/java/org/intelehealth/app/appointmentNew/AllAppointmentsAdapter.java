package org.intelehealth.app.appointmentNew;

import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Matrix4f;
import org.intelehealth.app.utilities.CustomLog;
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
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientAdapter_New;
import org.intelehealth.app.activities.visit.VisitDetailsActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
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

public class AllAppointmentsAdapter extends RecyclerView.Adapter<AllAppointmentsAdapter.MyViewHolder> {
    private static final String TAG = "MyAllAppointmentsAdapte";
    Context context;
    List<AppointmentInfo> appointmentsList;
    String whichAppointments ="";
    SessionManager sessionManager;

    public AllAppointmentsAdapter(Context context, List<AppointmentInfo> appointmentsList, String whichAppointments) {
        this.context = context;
        this.appointmentsList = appointmentsList;
        this.whichAppointments = whichAppointments;
        sessionManager = new SessionManager(context);
    }

    @Override
    public AllAppointmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_appointments_ui2_new, parent, false);
        AllAppointmentsAdapter.MyViewHolder myViewHolder = new AllAppointmentsAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AppointmentInfo appointmentInfoModel = appointmentsList.get(position);


        if (appointmentInfoModel.getPatientProfilePhoto() == null || appointmentInfoModel.getPatientProfilePhoto().equalsIgnoreCase("")) {
            if (NetworkConnection.isOnline(context)) {
                profilePicDownloaded(appointmentInfoModel, holder);
            }
        }

        // Set Age and Gender - start
       /* String age = DateAndTimeUtils.getAge_FollowUp(appointmentInfoModel.getPatientDob(), context);
        holder.search_gender.setText(appointmentInfoModel.getPatientGender() + " " + age);*/
        setGenderAgeLocal(context, holder.search_gender, appointmentInfoModel.getPatientDob(),
                appointmentInfoModel.getPatientGender(), sessionManager);

        // Set Age and Gender - end

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

        holder.doctNameTextView.setText(context.getString(R.string.doctor_annotation)+" " + appointmentInfoModel.getDrName());
        if (whichAppointments.equalsIgnoreCase("upcoming")) {
            //hide show ui elements bcz of common ui
            holder.cvPrescRx.setVisibility(View.GONE);
            holder.cvPrescPending.setVisibility(View.GONE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date());
            String slottime = appointmentInfoModel.getSlotDate() + " " + appointmentInfoModel.getSlotTime();

            long diff = 0;
            try {
                diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();
                long second = diff / 1000;
                long minutes = second / 60;
                CustomLog.v("AppointmentInfo", "Diff minutes - " + minutes);

                String timeText = "";
                //check for appointmet but presc not given and visit not completed
                if (minutes > 0) {
                    if (minutes >= 60) {
                        long hours = minutes / 60;
                        long mins = minutes % 60;
                        if (hours >= 24) {

                            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                            if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                                timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()) + ", " + context.getString(R.string.at) +" " + appointmentInfoModel.getSlotTime();
                            else if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                                timeText = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate())) + ", "  +  appointmentInfoModel.getSlotTime() + " " + context.getString(R.string.at);
                            holder.tvDate.setText(timeText);
                            holder.tvDate.setTextColor(context.getColor(R.color.iconTintGray));
                        } else {
                            if (hours > 1) {
                                if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                                    timeText = context.getString(R.string.in) + " " + hours + " " + context.getString(R.string.hours) + " " +
                                            mins + " " + context.getString(R.string.minutes_txt) + ", " +
                                            context.getString(R.string.at) + " " + appointmentInfoModel.getSlotTime();
                                else  if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                                    timeText = hours + " " + context.getString(R.string.hours) + " " + mins + " " + context.getString(R.string.minutes_txt) + " "
                                            + context.getString(R.string.in) + " , " + appointmentInfoModel.getSlotTime() + " " + context.getString(R.string.at);
                            }
                            else {
                                if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                                    timeText = context.getString(R.string.in) + " " + hours + " " + context.getString(R.string.hour) + " " +
                                            mins + " " + context.getString(R.string.minutes_txt) + ", " +
                                            context.getString(R.string.at) + " " + appointmentInfoModel.getSlotTime();
                                else  if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                                    timeText = hours + " " + context.getString(R.string.hours) + " " + mins + " " + context.getString(R.string.minutes_txt) + " "
                                            + context.getString(R.string.in) + " , " + appointmentInfoModel.getSlotTime() + " " + context.getString(R.string.at);
                            }
                            holder.tvDate.setText(timeText);
                            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                            holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                        }
                    }
                    else {
                        if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                            timeText = context.getString(R.string.in) + " " + minutes + " " + context.getString(R.string.minutes_txt);
                        else if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                            timeText = minutes + " " + context.getString(R.string.minutes_txt) + " " + context.getString(R.string.in) ;

                        holder.tvPatientName.setText(appointmentInfoModel.getPatientName());

                        holder.tvDate.setText(timeText);
                        holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                    }
                }


            } catch (ParseException e) {
                CustomLog.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
                e.printStackTrace();
            }

        }
        CustomLog.d(TAG, "onBindViewHolder: whichAppointments : "+whichAppointments);
        try {
            if (whichAppointments.equalsIgnoreCase("completed")) {
                CustomLog.d(TAG, "onBindViewHolder: in completed");
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                holder.tvDate.setText(DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time(), context));
                CustomLog.d(TAG, "onBindViewHolder: presc time : "+appointmentInfoModel.getPresc_received_time());

                if (appointmentInfoModel.isPrescription_exists()) {
                    holder.cvPrescRx.setVisibility(View.VISIBLE);
                    holder.cvPrescPending.setVisibility(View.GONE);
                } else {
                    holder.tvDate.setVisibility(View.GONE);
                    holder.cvPrescPending.setVisibility(View.VISIBLE);
                    holder.cvPrescRx.setVisibility(View.GONE);
                }
            }


            holder.cardParent.setOnClickListener(v -> {
                Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                context.startActivity(intent);
            });

        } catch (Exception e) {
            CustomLog.d(TAG, "onBindViewHolder: e main : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        if (whichAppointments.equalsIgnoreCase("cancelled")) {
            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
            //holder.ivTime.setVisibility(View.VISIBLE);
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.cvPrescRx.setVisibility(View.GONE);
            holder.cvPrescPending.setVisibility(View.GONE);

           String timeText = "";
            if(sessionManager.getAppLanguage().equalsIgnoreCase("en"))
                timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()) + "," + context.getString(R.string.at) +" " + appointmentInfoModel.getSlotTime();
            else if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                timeText = StringUtils.en_hi_dob_updated(DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate())) + ","  + " " + appointmentInfoModel.getSlotTime() + " " + context.getString(R.string.at);
            holder.tvDate.setText(timeText);
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
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardParent, cvPrescPending, cvPrescRx;
        ImageView ivProfileImage;
        LinearLayout IvPriorityTag;
        TextView tvPatientName, tvDate, tvPrescRecStatus,doctNameTextView, search_gender;


        public MyViewHolder(View itemView) {
            super(itemView);
            cardParent = itemView.findViewById(R.id.card_all_appointments_all);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name_all);
            ivProfileImage = itemView.findViewById(R.id.profile_image_all);
            tvDate = itemView.findViewById(R.id.tv_date_appointment_all);
            IvPriorityTag = itemView.findViewById(R.id.llPriorityTagAllAppointmentItem);
            cvPrescPending = itemView.findViewById(R.id.cvPrescPendingAllAppointment);
            cvPrescRx = itemView.findViewById(R.id.cvPrescRxAllAppointment);
            doctNameTextView = itemView.findViewById(R.id.tv_dr_name_todays);
            search_gender = itemView.findViewById(R.id.search_gender);

        }
    }

    public void profilePicDownloaded(AppointmentInfo model, MyViewHolder holder) {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
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

   /* public void searchForFilters(String appointmentType,
                                           String date, String fromWhere, String test) {
        CustomLog.d(TAG, "searchForProductAndVehicle:111 fromWhere : " + fromWhere);
        CustomLog.d(TAG, "searchForProductAndVehicle:111 product : " + product);
        CustomLog.d(TAG, "searchForProductAndVehicle:111 vehicleNo : " + vehicleNo);

        if (vehicleNo.isEmpty() && product.isEmpty()) {
            fleetListModelList.addAll(alltxnsModelList);
            notifyDataSetChanged();
        }

        //for only vehicle number
        if (!vehicleNo.isEmpty()) {
            fleetListModelList.clear();
            for (ArrivallistModel arrivallistModel : alltxnsModelList) {
                if (arrivallistModel.getVehicleNo().toUpperCase().trim().contains(vehicleNo.toUpperCase().trim())) {
                    fleetListModelList.add(arrivallistModel);
                }
            }
        }

        //for only product
        if (!product.isEmpty()) {
            fleetListModelList.clear();

            for (ArrivallistModel arrivallistModel : alltxnsModelList) {
                CustomLog.d(TAG, "searchForProductAndVehicle:listalldata " + arrivallistModel.getProductName());

                if (arrivallistModel.getProductName().toUpperCase().trim().equals(product.toUpperCase().trim())) {
                    CustomLog.d(TAG, "searchForProductAndVehicle: check : ");
                    CustomLog.d(TAG, "searchForProductAndVehicle: 11ch : from list : " + arrivallistModel.getProductName());
                    CustomLog.d(TAG, "searchForProductAndVehicle: 11ch : from search : " + product.toUpperCase());

                    fleetListModelList.add(arrivallistModel);
                }
            }
        }
        //for vehicle and product
        if (!vehicleNo.isEmpty() && !product.isEmpty()) {
            fleetListModelList.clear();
            for (ArrivallistModel arrivallistModel : alltxnsModelList) {
                if (arrivallistModel.getVehicleNo().toUpperCase().contains(vehicleNo.toUpperCase().trim()) && arrivallistModel.getProductName().toUpperCase().trim().equals(product.toUpperCase().trim())) {
                    fleetListModelList.add(arrivallistModel);
                }
            }

        }
        notifyDataSetChanged();

    }*/
}
