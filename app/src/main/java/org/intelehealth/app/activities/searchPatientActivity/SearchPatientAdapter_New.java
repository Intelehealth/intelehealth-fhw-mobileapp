package org.intelehealth.app.activities.searchPatientActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientAdapter_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

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
    private List<PatientDTO> patientDTOS;
    private String profileImage = "";
    String profileImage1 = "";
    private ImagesDAO imagesDAO = new ImagesDAO();
    SessionManager sessionManager;

    public SearchPatientAdapter_New(Context context, List<PatientDTO> patientDTOS) {
        this.context = context;
        this.patientDTOS = patientDTOS;
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
        if (model != null) {
            //  1. Age
            String age = DateAndTimeUtils.getAge_FollowUp(model.getDateofbirth(), context);
            holder.search_gender.setText(model.getGender() + " " + age);

            //  2. Name
            holder.search_name.setText(model.getFirstname() + " " + model.getLastname());

            //  3. Priority Tag
            if (model.isEmergency())
                holder.priority_tag_imgview.setVisibility(View.VISIBLE);
            else
                holder.priority_tag_imgview.setVisibility(View.GONE);

            //  4. Visit Start Date else No visit created text display.
            if (model.getVisit_startdate() != null) {
                holder.fu_item_calendar.setVisibility(View.VISIBLE);
                holder.search_date_relative.setText(model.getVisit_startdate());
            } else {
                holder.fu_item_calendar.setVisibility(View.GONE);
                holder.search_date_relative.setText(R.string.no_visit_created);
            }

            //  5. Prescription received/pending tag display.
            if (model.isPrescription_exists())
                holder.presc_tag_imgview.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_presc_received));
            else
                holder.presc_tag_imgview.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_presc_pending));

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
                Glide.with(context)
                        .load(model.getPatientPhoto())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.profile_imgview);
            }
            else {
                holder.profile_imgview.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }

        }
    }

    @Override
    public int getItemCount() {
        return patientDTOS.size();
    }

    public class SearchHolderView extends RecyclerView.ViewHolder {
        TextView search_gender, search_name, search_date_relative;
        ImageView priority_tag_imgview, fu_item_calendar, presc_tag_imgview, profile_imgview;

        public SearchHolderView(@NonNull View itemView) {
            super(itemView);

            search_gender = itemView.findViewById(R.id.search_gender);
            search_name = itemView.findViewById(R.id.search_name);
            priority_tag_imgview = itemView.findViewById(R.id.priority_tag_imgview);
            fu_item_calendar = itemView.findViewById(R.id.fu_item_calendar);
            search_date_relative = itemView.findViewById(R.id.search_date_relative);
            presc_tag_imgview = itemView.findViewById(R.id.presc_tag_imgview);
            profile_imgview = itemView.findViewById(R.id.profile_imgview);
        }
    }

    public void profilePicDownloaded(PatientDTO model, SearchPatientAdapter_New.SearchHolderView holder) {
        sessionManager = new SessionManager(context);
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
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg")
                                    .thumbnail(0.3f)
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
