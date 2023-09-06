package org.intelehealth.app.activities.additionalDocumentsActivity;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.intelehealth.app.R;


/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentViewHolder extends RecyclerView.ViewHolder{

    private TextView documentNameTextView;
    private ImageView documentPhotoImageView;
    private ImageButton deleteDocumentImageView;
    private View rootView;

    public AdditionalDocumentViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        documentNameTextView = itemView.findViewById(R.id.tvDocNameAddDocsListItem);
        documentPhotoImageView = itemView.findViewById(R.id.ivDocPhotoAddDocsListItem);
        deleteDocumentImageView = itemView.findViewById(R.id.ibDocDeletedAddDocsListItem);
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
