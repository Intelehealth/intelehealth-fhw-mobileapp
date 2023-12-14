package org.intelehealth.kf.activities.prescription;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.kf.R;
import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.networkApiCalls.ApiClient;
import org.intelehealth.kf.networkApiCalls.ApiInterface;
import org.intelehealth.kf.utilities.Base64Utils;
import org.intelehealth.kf.utilities.SessionManager;
import org.intelehealth.kf.utilities.UrlModifiers;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 17:01
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TestPrescAdapter extends RecyclerView.Adapter<TestPrescAdapter.PrescViewModel> {
    Context context;
    List<PrescDataModel> prescDataModels;
    SessionManager sessionManager;
    Base64Utils base64Utils = new Base64Utils();

    public TestPrescAdapter(Context context, List<PrescDataModel> prescDataModels) {
        this.context = context;
        this.prescDataModels = prescDataModels;
        sessionManager = new SessionManager(this.context);
    }

    public TestPrescAdapter(Context presContext) {
        this.context = presContext;
    }

    @Override
    public PrescViewModel onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.presc_selection_tab, parent, false);

        return new PrescViewModel(view);
    }

    @Override
    public void onBindViewHolder(PrescViewModel holder, int position) {
        if(prescDataModels.size() > 0)
            holder.userSelectionValueTextview.setText(prescDataModels.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return prescDataModels.size();
    }

    public class PrescViewModel extends RecyclerView.ViewHolder {
        TextView userSelectionValueTextview;
        ImageView deleteImageButton;
        String uuid;

        public PrescViewModel(View itemView) {
            super(itemView);
            userSelectionValueTextview = itemView.findViewById(R.id.userSelectionValueTextview);
            deleteImageButton = itemView.findViewById(R.id.deleteImageButton);

            deleteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteObsItem();
                }
            });
        }

        private void deleteObsItem() {
            if(prescDataModels.size() > 0) {
                UrlModifiers urlModifiers = new UrlModifiers();
                int clickedPosition = getAdapterPosition();
                String uuid = prescDataModels.get(clickedPosition).getUuid();
                Log.v("index", "index1: " + clickedPosition);
                String url = urlModifiers.setDeletePrescItemUrl(uuid);
                //  String encoded = sessionManager.getEncoded();
                String encoded = base64Utils.encoded("sysnurse", "Nurse123");

                ApiInterface apiService = ApiClient.createService(ApiInterface.class);
                Observable<Response<Void>> responseBodyObservable = apiService.DELETE_PRESCOBS_ITEM(url, "Basic " + encoded);
                responseBodyObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<Response<Void>>() {
                            @Override
                            public void onNext(@NonNull Response<Void> avoid) {
                                // Delete is successful from backend. Now, Delete from Recyclerview.
                                Log.v("index", "index2: " + clickedPosition);
                                prescDataModels.remove(clickedPosition);
                                notifyItemRemoved(clickedPosition);
                                notifyItemRangeChanged(clickedPosition, prescDataModels.size());

                                //Delete from local db as well.
                                String encounterVisitNoteUuid = prescDataModels.get(getAdapterPosition()).encounterVisitNoteUuid();
                                String conceptuuid = prescDataModels.get(getAdapterPosition()).getConceptUuid();
                                deleteObsDBItem(uuid, encounterVisitNoteUuid, conceptuuid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.v("index", "error: "+e);
                            }

                            @Override
                            public void onComplete() {
                                Log.v("index", "complete: ");
                            }
                        });
            }
        }

        private void deleteObsDBItem(String obsuuid, String encounterVisitNoteUuid, String conceptUuid) {
            SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
            db.beginTransaction();

            String tablename = "tbl_obs";
            String whereClause = "uuid=? AND encounteruuid=? AND conceptuuid=?";
            String[] whereArgs = new String[] { obsuuid, encounterVisitNoteUuid, conceptUuid };
            db.delete(tablename, whereClause, whereArgs);
            if(db != null)
                db.close();
        }

    }
}
