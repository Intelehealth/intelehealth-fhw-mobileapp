package org.intelehealth.app.activities.searchPatientActivity;

import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by Prajwal Waingankar on 20/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class SearchPatientAdapter_New extends RecyclerView.Adapter<SearchPatientAdapter_New.SearchHolderView> {
    private Context context;
    List<PatientDTO> patientDTOS = new ArrayList<>();
    private String profileImage = "";
    String profileImage1 = "";
    private ImagesDAO imagesDAO = new ImagesDAO();
    SessionManager sessionManager;

    public SearchPatientAdapter_New(Context context, List<PatientDTO> patientDTOS) {
        this.context = context;
        this.patientDTOS.addAll(patientDTOS);
        sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public SearchPatientAdapter_New.SearchHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.search_listitem_layout, parent, false);
        return new SearchPatientAdapter_New.SearchHolderView(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter_New.SearchHolderView holder, int position) {
        final PatientDTO model = patientDTOS.get(position);
        holder.patientDTO = model;
        if (model != null) {

            //  1. Age
            /*String age = DateAndTimeUtils.getAge_FollowUp(model.getDateofbirth(), context);
            holder.search_gender.setText(model.getGender() + " " + age);*/
            setGenderAgeLocal(context, holder.search_gender, model.getDateofbirth(), model.getGender(), sessionManager);

            //  2. Name
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname());

            //  3. Priority Tag
            if (model.isEmergency())
                holder.fl_priority.setVisibility(View.VISIBLE);
            else
                holder.fl_priority.setVisibility(View.GONE);

            //  4. Visit Start Date else No visit created text display.
            if (model.getVisit_startdate() != null) {
                if (model.isPrescription_exists()) {
                    holder.presc_receivingCV.setVisibility(View.VISIBLE);
                    holder.presc_pendingCV.setVisibility(View.GONE);
                } else if (!model.isPrescription_exists()) {
                    holder.presc_pendingCV.setVisibility(View.VISIBLE);
                    holder.presc_receivingCV.setVisibility(View.GONE);
                }

                //  5. Checking visit uploaded or not and Prescription received/pending tag display. - start
                if (model.getVisitDTO() != null) {
                    if (model.getVisitDTO().getSyncd() != null && model.getVisitDTO().getSyncd()) {
                        //holder.visitNotUploadCV.setVisibility(View.GONE);
                    } else {
                        //holder.visitNotUploadCV.setVisibility(View.VISIBLE);
                        holder.presc_pendingCV.setVisibility(View.GONE);
                        holder.presc_receivingCV.setVisibility(View.GONE);
                    }

                    if (model.getVisitDTO().getEnddate() != null) {
                        //holder.visitNotUploadCV.setVisibility(View.GONE);
                    }
                }
                // checking visit uploaded or not - end

                holder.fu_item_calendar.setVisibility(View.VISIBLE);
                String visitDate = model.getVisit_startdate();
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    visitDate = StringUtils.en_hi_dob_three(visitDate);
                holder.search_date_relative.setVisibility(View.VISIBLE);
                holder.search_date_relative.setText(visitDate);
            } else {
                holder.presc_pendingCV.setVisibility(View.GONE);
                holder.presc_receivingCV.setVisibility(View.GONE);

                holder.fu_item_calendar.setVisibility(View.GONE);
                //holder.search_date_relative.setText(R.string.no_visit_created);
                holder.search_date_relative.setVisibility(View.GONE);
                holder.fu_item_calendar.setVisibility(View.GONE);
            }

            //  6. Patient Profile Pic
            //1.
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(model.getUuid());
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            //2.
            if (model.getPatientPhoto() == null || model.getPatientPhoto().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(context)) {
                    profilePicDownloaded(model, holder);
                }
            }
            //3.
            if (!profileImage.equalsIgnoreCase(profileImage1)) {
                if (NetworkConnection.isOnline(context)) {
                    profilePicDownloaded(model, holder);
                }
            }

            if (model.getPatientPhoto() != null) {
                RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                        .asDrawable().sizeMultiplier(0.3f);
                Glide.with(context)
                        .load(model.getPatientPhoto())
                        .thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.profile_imgview);
            } else {
                holder.profile_imgview.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.avatar1));
            }

        }
    }

    @Override
    public int getItemCount() {
        return patientDTOS.size();
    }

    public class SearchHolderView extends RecyclerView.ViewHolder {
        TextView search_gender, search_name, search_date_relative;
        ImageView fu_item_calendar, profile_imgview;
        LinearLayout priority_tag_imgview;
        PatientDTO patientDTO;
        CardView presc_pendingCV, presc_receivingCV, visitNotUploadCV;
        FrameLayout fl_priority;

        public SearchHolderView(@NonNull View itemView) {
            super(itemView);

            search_gender = itemView.findViewById(R.id.search_gender);
            search_name = itemView.findViewById(R.id.search_name);
            priority_tag_imgview = itemView.findViewById(R.id.llPriorityTagSearchPatientListItem);
            fl_priority = itemView.findViewById(R.id.fl_priority);
            fu_item_calendar = itemView.findViewById(R.id.fu_item_calendar);
            search_date_relative = itemView.findViewById(R.id.search_date_relative);
            profile_imgview = itemView.findViewById(R.id.profile_imgview);
            presc_pendingCV = itemView.findViewById(R.id.presc_pending_CV);
            presc_receivingCV = itemView.findViewById(R.id.presc_received_CV);
            visitNotUploadCV = itemView.findViewById(R.id.presc_visit_not_uploaded_CV);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PatientDetailActivity2.class);
                    intent.putExtra("patientUuid", patientDTO.getUuid());
                    intent.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                    intent.putExtra("tag", "searchPatient");
                    intent.putExtra("hasPrescription", "false");
                    //   i.putExtra("privacy", privacy_value); // todo: uncomment later.
                    //   CustomLog.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    Bundle args = new Bundle();
                    args.putSerializable("patientDTO", (Serializable) patientDTO);
                    intent.putExtra("BUNDLE", args);
                    intent.putExtra("patientUuid", patientDTO.getUuid());
                    context.startActivity(intent);
                }
            });
        }
    }

    public void profilePicDownloaded(PatientDTO model, SearchPatientAdapter_New.SearchHolderView holder) {
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
                        Logger.logD("TAG", "complete" + model.getPatientPhoto());
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
                                    .into(holder.profile_imgview);
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
