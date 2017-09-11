package io.intelehealth.client.activities.sync_activity.sync_adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.intelehealth.client.R;
import io.intelehealth.client.services.ClientService;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class SyncViewHolder extends RecyclerView.ViewHolder {

    private TextView headTextView;
    private TextView bodyTextView;
    private TextView indicatorTextView;
    private View rootView;

    public SyncViewHolder(View itemView) {
        super(itemView);
        headTextView = (TextView) itemView.findViewById(R.id.list_item_head_text_view);
        bodyTextView = (TextView) itemView.findViewById(R.id.list_item_body_text_view);
        indicatorTextView = (TextView) itemView.findViewById(R.id.list_item_indicator_text_view);
        rootView = itemView;
    }

    public TextView getHeadTextView() {
        return headTextView;
    }

    public TextView getBodyTextView() {
        return bodyTextView;
    }

    public TextView getIndicatorTextView() {
        return indicatorTextView;
    }

    public View getRootView() {
        return rootView;
    }

    public void bindItems(SyncModel syncModel) {

        String serviceName;
        switch (syncModel.getJobType()) {
            case "patient": {
                serviceName = "Uploading Patient Data";
                break;
            }
            case "visit": {
                serviceName = "Uploading Visit Data";
                break;
            }
            case "endVisit": {
                serviceName = "Uploading End Visit";
                break;
            }
            case "photoUpload": {
                serviceName = "UploadingPatient Photo";
                break;
            }
            case "imageUpload": {
                serviceName = "Uploading Visit Images";
                break;
            }
            case "prescriptionDownload": {
                serviceName = "Downloading Prescription";
                break;
            }
            case "obsUpdate": {
                serviceName = "Uploading Visit Updates";
                break;
            }
            case "patientUpdate": {
                serviceName = "Uploading Patient Updates";
                break;
            }
            default:
                serviceName = "";
        }

        getHeadTextView().setText(serviceName);

        getBodyTextView().setText(syncModel.getPatientName() + "\n" +
                getBodyTextView().getContext().getString(R.string.id_number) + ":" +
                syncModel.getPatientId());

        switch (syncModel.getSyncStatus()) {
            case ClientService.STATUS_SYNC_STOPPED: {
                getIndicatorTextView().setText(getRootView().getContext()
                        .getString(R.string.sync_stopped));
                break;
            }
            case ClientService.STATUS_SYNC_IN_PROGRESS: {
                getIndicatorTextView().setText(getRootView().getContext()
                        .getString(R.string.sync_in_progress));
                break;
            }
            default: {
                getIndicatorTextView().setText(getRootView().getContext().
                        getString(R.string.sync_error));
            }
        }

    }
}
