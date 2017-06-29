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

import static android.support.constraint.R.id.parent;

/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentAdapter extends RecyclerView.Adapter<AdditionalDocumentViewHolder> {

    private List<DocumentObject> documentList;
    private Context context;

    private AdditionalDocumentContract contract;

    public AdditionalDocumentAdapter(Context context, List<DocumentObject> documentList) {
        this.documentList = documentList;
        this.context = context;
        if(context instanceof AdditionalDocumentContract){
            contract = (AdditionalDocumentContract) context;
        }else{

        }
    }

    @Override
    public AdditionalDocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_additional_doc, null);
        AdditionalDocumentViewHolder rcv = new AdditionalDocumentViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(AdditionalDocumentViewHolder holder, int position) {
        holder.documentName.setText(documentList.get(position).getDocumentName());
        final File image = new File(documentList.get(position).getDocumentPhoto());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        holder.documentPhoto.setImageBitmap(bitmap);

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromFile(image)));
            }
        });

        holder.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });
    }



    @Override
    public int getItemCount() {
        return this.documentList.size();
    }
}
