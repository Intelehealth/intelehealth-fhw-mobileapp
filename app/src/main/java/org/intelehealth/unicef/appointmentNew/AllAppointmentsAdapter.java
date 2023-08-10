package org.intelehealth.unicef.appointmentNew;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.appointment.model.AppointmentInfo;
import org.intelehealth.unicef.database.dao.ImagesDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.DownloadFilesUtils;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.UrlModifiers;
import org.intelehealth.unicef.utilities.exception.DAOException;

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
    String whichAppointments = "";

    public AllAppointmentsAdapter(Context context, List<AppointmentInfo> appointmentsList, String whichAppointments) {
        this.context = context;
        this.appointmentsList = appointmentsList;
        this.whichAppointments = whichAppointments;

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


        if (appointmentInfoModel.getPatientProfilePhoto() != null && !appointmentInfoModel.getPatientProfilePhoto().isEmpty()) {
            Glide.with(context)
                    .load(appointmentInfoModel.getPatientProfilePhoto())
                    .thumbnail(0.3f)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.ivProfileImage);
        } else {
            holder.ivProfileImage.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
        }

        holder.doctNameTextView.setText(context.getString(R.string.doctor_annotation) + " " + appointmentInfoModel.getDrName());
        if (whichAppointments.equalsIgnoreCase("upcoming")) {
            //hide show ui elements bcz of common ui
//            holder.tvPrescRecStatus.setVisibility(View.GONE);
            holder.rlPrescriptionBackground.setVisibility(View.GONE);
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
                if (minutes > 0) {
                    if (minutes >= 60) {
                        long hours = minutes / 60;
                        if (hours > 12) {

                            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                            //holder.ivTime.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_calendar));
                            //holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.iconTintGray), PorterDuff.Mode.SRC_IN);

                            timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()) + "," + context.getString(R.string.at) + " " + appointmentInfoModel.getSlotTime();
                            holder.tvDate.setText(timeText);
                            holder.tvDate.setTextColor(context.getColor(R.color.iconTintGray));
                        } else {
                            timeText = context.getString(R.string.in) + " " + hours + " " + context.getString(R.string.hours_at) + " " + appointmentInfoModel.getSlotTime();
                            //holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary1), PorterDuff.Mode.SRC_IN);
                            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                            holder.tvDate.setText(timeText);
                            holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                        }
                    } else {
                        timeText = context.getString(R.string.in) + " " + minutes + " " + context.getString(R.string.minutes_txt);
                        //holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary1), PorterDuff.Mode.SRC_IN);
                        holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                        holder.tvDate.setText(timeText);
                        holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                    }
                }


            } catch (ParseException e) {
                Log.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
                e.printStackTrace();
            }

        }
        Log.d(TAG, "onBindViewHolder: whichAppointments : " + whichAppointments);
        try {
            if (whichAppointments.equalsIgnoreCase("completed")) {
                Log.d(TAG, "onBindViewHolder: in completed");
                //bcz of common UI
                //hide  : ivTime, tvDate, tvPatientId
                //show :  tvPrescRecStatus

                //holder.ivTime.setVisibility(View.VISIBLE);
                holder.tvDate.setVisibility(View.GONE);
//                holder.tvPrescRecStatus.setVisibility(View.VISIBLE);
                holder.rlPrescriptionBackground.setVisibility(View.VISIBLE);
                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                String timeText = DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time());
                holder.tvDate.setText(timeText);
                Log.d(TAG, "onBindViewHolder: presc time : " + appointmentInfoModel.getPresc_received_time());

                if (appointmentInfoModel.isPrescription_exists()) {
//                    holder.tvPrescRecStatus.setBackground(context.getResources().getDrawable(R.drawable.ui2_ic_presc_received));
                    holder.tvPrescRecStatus.setText(context.getResources().getText(R.string.tag_prescription_received));
                    holder.rlPrescriptionBackground.setBackground(context.getDrawable(R.drawable.pres_received_drawable_rounded_corners));
                } else {
                    //holder.ivTime.setVisibility(View.GONE);
//                    holder.tvDate.setVisibility(View.GONE);
//                    holder.tvPrescRecStatus.setBackground(context.getResources().getDrawable(R.drawable.ui2_ic_presc_pending));
                    holder.tvPrescRecStatus.setText(context.getResources().getText(R.string.tag_prescription_pending));
                    holder.rlPrescriptionBackground.setBackground(context.getDrawable(R.drawable.pres_pending_drawable_rounded_corners));
                }
            }


            holder.cardParent.setOnClickListener(v -> {
                Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                context.startActivity(intent);
            });

        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: e main : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        if (whichAppointments.equalsIgnoreCase("cancelled")) {
            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
            //holder.ivTime.setVisibility(View.VISIBLE);
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvPrescRecStatus.setVisibility(View.GONE);
            holder.rlPrescriptionBackground.setVisibility(View.GONE);

            String timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()) + "," + context.getString(R.string.at) + " " + appointmentInfoModel.getSlotTime();
            holder.tvDate.setText(timeText);
            //  holder.ivTime.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_calendar));
            // imageView.setColorFilter(ContextCompat.getColor(context, R.color.COLOR_YOUR_COLOR), android.graphics.PorterDuff.Mode.SRC_IN);

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
                intent.putExtra("prescription_received_time", DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time()));
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
        CardView cardParent;
        TextView tvPatientName, tvDate, tvPrescRecStatus, doctNameTextView, tvPresStatus, IvPriorityTag;
        ImageView ivProfileImage /*, ivTime*/;
        RelativeLayout rlPrescriptionBackground;


        public MyViewHolder(View itemView) {
            super(itemView);
            cardParent = itemView.findViewById(R.id.card_all_appointments_all);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name_all);
            ivProfileImage = itemView.findViewById(R.id.profile_image_all);
            tvDate = itemView.findViewById(R.id.tv_date_appointment_all);
            IvPriorityTag = itemView.findViewById(R.id.iv_priority_tag1_all);
            //ivTime = itemView.findViewById(R.id.iv_time_all);
            tvPrescRecStatus = itemView.findViewById(R.id.tv_prescription_states_all);
            doctNameTextView = itemView.findViewById(R.id.tv_dr_name_todays);
            tvPrescRecStatus = itemView.findViewById(R.id.pres_status_text);
            rlPrescriptionBackground = itemView.findViewById(R.id.prescription_background);
        }
    }

    public void profilePicDownloaded(AppointmentInfo model, MyViewHolder holder) {
        SessionManager sessionManager = new SessionManager(context);
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
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg")
                                    .thumbnail(0.3f)
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
        Log.d(TAG, "searchForProductAndVehicle:111 fromWhere : " + fromWhere);
        Log.d(TAG, "searchForProductAndVehicle:111 product : " + product);
        Log.d(TAG, "searchForProductAndVehicle:111 vehicleNo : " + vehicleNo);

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
                Log.d(TAG, "searchForProductAndVehicle:listalldata " + arrivallistModel.getProductName());

                if (arrivallistModel.getProductName().toUpperCase().trim().equals(product.toUpperCase().trim())) {
                    Log.d(TAG, "searchForProductAndVehicle: check : ");
                    Log.d(TAG, "searchForProductAndVehicle: 11ch : from list : " + arrivallistModel.getProductName());
                    Log.d(TAG, "searchForProductAndVehicle: 11ch : from search : " + product.toUpperCase());

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
