package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class EndVisitAdapter extends RecyclerView.Adapter<EndVisitAdapter.Myholder> {
    private Context context;
    private List<PrescriptionModel> arrayList;
    ImagesDAO imagesDAO = new ImagesDAO();
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager;

    public EndVisitAdapter(Context context, List<PrescriptionModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public EndVisitAdapter.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new EndVisitAdapter.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull EndVisitAdapter.Myholder holder, int position) {
        PrescriptionModel model = arrayList.get(position);
        if (model != null) {
            // name
            holder.name.setText(model.getFirst_name() + " " + model.getLast_name());

            // Patient Photo
            //1.
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(model.getPatientUuid());
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            //2.
            if (model.getPatient_photo() == null || model.getPatient_photo().equalsIgnoreCase("")) {
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

            if (model.getPatient_photo() != null) {
                Glide.with(context)
                        .load(model.getPatient_photo())
                        .override(100, 100)
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.profile_image);
            } else {
                holder.profile_image.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }
            // photo - end

            // start date show
            holder.fu_date_txtview.setText(model.getVisit_start_date());

            holder.end_visit_btn.setOnClickListener(v -> {
                showConfirmDialog(model);
            });
        }
    }

    private void showConfirmDialog(final PrescriptionModel model) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(context, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.confirm_end_visit_reason), context.getResources().getString(R.string.confirm_end_visit_reason_message), false, context.getResources().getString(R.string.confirm), context.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    String vitalsUUID = fetchEncounterUuidForEncounterVitals(model.getVisitUuid());
                    String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(model.getVisitUuid());

                    VisitUtils.endVisit(context, model.getVisitUuid(), model.getPatientUuid(), model.getFollowup_date(),
                            vitalsUUID, adultInitialUUID, "state",
                            model.getFirst_name() + " " + model.getLast_name().substring(0, 1), "VisitDetailsActivity");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        Button end_visit_btn;
        private CardView fu_cardview_item;
        private TextView name, fu_date_txtview;
        private ImageView profile_image;
        private LinearLayout shareicon;


        public Myholder(@NonNull View itemView) {
            super(itemView);
            end_visit_btn = itemView.findViewById(R.id.end_visit_btn);
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item);
            name = itemView.findViewById(R.id.fu_patname_txtview);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            profile_image = itemView.findViewById(R.id.profile_image);
            shareicon = itemView.findViewById(R.id.shareiconLL);

            end_visit_btn.setVisibility(View.VISIBLE);
        }
    }

    public void profilePicDownloaded(PrescriptionModel model, EndVisitAdapter.Myholder holder) {
        sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getPatientUuid());
        Logger.logD("TAG", "profileimage url" + url);

        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, model.getPatientUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + model.getPatient_photo());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(model.getPatientUuid(),
                                    AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg")
                                    .override(100, 100)
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(holder.profile_image);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg", model.getPatientUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

}
