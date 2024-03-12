package org.intelehealth.app.activities.additionalDocumentsActivity;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.intelehealth.app.R;


/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentViewHolder extends RecyclerView.ViewHolder {

    private TextView documentNameTextView;
    private ImageView documentPhotoImageView;
    private ImageView deleteDocumentImageView;
    private View rootView;

    public AdditionalDocumentViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        documentNameTextView = itemView.findViewById(R.id.document_name_TextView);
        documentPhotoImageView = itemView.findViewById(R.id.document_photo_ImageView);
        deleteDocumentImageView = itemView.findViewById(R.id.document_delete_button_ImageView);
    }

    public TextView getDocumentNameTextView() {
        return documentNameTextView;
    }

    public ImageView getDocumentPhotoImageView() {
        return documentPhotoImageView;
    }

    public ImageView getDeleteDocumentImageView() {
        return deleteDocumentImageView;
    }

    public View getRootView() {
        return rootView;
    }

    public void hideCancel(boolean flag) {
        int visibility = flag ? View.GONE : View.VISIBLE;
        deleteDocumentImageView.setVisibility(visibility);
    }

}
