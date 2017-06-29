package io.intelehealth.client.activities.additional_documents_activity;

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

    public TextView documentName;
    public ImageView documentPhoto;
    public View rootView;

    public AdditionalDocumentViewHolder(View itemView) {
        super(itemView);

        rootView = itemView;
        documentName = (TextView)itemView.findViewById(R.id.document_name);
        documentPhoto = (ImageView)itemView.findViewById(R.id.document_photo);
    }

    public TextView getDocumentName() {
        return documentName;
    }

    public ImageView getDocumentPhoto() {
        return documentPhoto;
    }

    public View getRootView() {
        return rootView;
    }
}
