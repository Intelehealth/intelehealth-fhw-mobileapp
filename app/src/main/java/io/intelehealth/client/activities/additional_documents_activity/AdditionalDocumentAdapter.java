package io.intelehealth.client.activities.additional_documents_activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

import io.intelehealth.client.R;

/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentAdapter extends RecyclerView.Adapter<AdditionalDocumentViewHolder> {

    private List<DocumentObject> documentList;
    private Context context;


    public AdditionalDocumentAdapter(Context context, List<DocumentObject> documentList) {
        this.documentList = documentList;
        this.context = context;

    }

    @Override
    public AdditionalDocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_additional_doc, null);
        AdditionalDocumentViewHolder rcv = new AdditionalDocumentViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(AdditionalDocumentViewHolder holder, final int position) {
        holder.getDocumentNameTextView().setText(documentList.get(position).getDocumentName());
        final File image = new File(documentList.get(position).getDocumentPhoto());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        holder.getDocumentPhotoImageView().setImageBitmap(bitmap);

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromFile(image)));
            }
        });

        holder.getDeleteDocumentImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image.delete();
                documentList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return this.documentList.size();
    }

    public void addDocumentToList(DocumentObject documentObject){
        documentList.add(documentObject);
        notifyDataSetChanged();
    }
}
