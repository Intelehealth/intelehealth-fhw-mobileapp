package io.intelehealth.client.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.intelehealth.client.R;

/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentViewHolder extends RecyclerView.ViewHolder{

    private TextView documentNameTextView;
    private ImageView documentPhotoImageView;
    private ImageView deleteDocumentImageView;
    private View rootView;

    public AdditionalDocumentViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        documentNameTextView = (TextView)itemView.findViewById(R.id.document_name_TextView);
        documentPhotoImageView = (ImageView)itemView.findViewById(R.id.document_photo_ImageView);
        deleteDocumentImageView = (ImageView)itemView.findViewById(R.id.document_delete_button_ImageView);
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

}
