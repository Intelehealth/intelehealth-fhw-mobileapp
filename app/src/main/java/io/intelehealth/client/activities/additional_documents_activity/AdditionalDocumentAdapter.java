package io.intelehealth.client.activities.additional_documents_activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;

/**
 * Created by Dexter Barretto on 6/28/17.
 * Github : @dbarretto
 */

public class AdditionalDocumentAdapter extends RecyclerView.Adapter<AdditionalDocumentViewHolder> {

    private List<DocumentObject> documentList = new ArrayList<>();
    private Context context;
    private String filePath;

    private static final String TAG = AdditionalDocumentAdapter.class.getSimpleName();

    public AdditionalDocumentAdapter(Context context, List<DocumentObject> documentList,String filePath) {
        this.documentList = documentList;
        this.context = context;
        this.filePath = filePath;
    }

    @Override
    public AdditionalDocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_additional_doc, null);
        AdditionalDocumentViewHolder rcv = new AdditionalDocumentViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final AdditionalDocumentViewHolder holder, final int position) {
        holder.getDocumentNameTextView().setText(documentList.get(position).getDocumentName());
        final File image = new File(documentList.get(position).getDocumentPhoto());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        final Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        holder.getDocumentPhotoImageView().setImageBitmap(bitmap);

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImage(bitmap);
            }
        });

        holder.getDeleteDocumentImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(image.exists()) image.delete();
                documentList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,documentList.size());
                String imageName = holder.getDocumentNameTextView().getText().toString();
                String dir = filePath + File.separator + imageName;
                deleteImageFromDatabase(dir);
            }
        });
    }

    private void deleteImageFromDatabase(String imagePath) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(context);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        localdb.execSQL("DELETE FROM image_records WHERE image_path=" + "'" + imagePath + "'");
    }


    public void add(DocumentObject doc){
        boolean bool = documentList.add(doc);
        if (bool) Log.d(TAG, "add: Item added to list");
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.documentList.size();
    }


    public void displayImage(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ImageView imageView = (ImageView) dialog.findViewById(R.id.confirmationImageView);
                float imageWidthInPX = (float) imageView.getWidth();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                        Math.round(imageWidthInPX * (float) bitmap.getHeight() / (float) bitmap.getWidth()));
                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
            }
        });

        dialog.show();

    }
}
