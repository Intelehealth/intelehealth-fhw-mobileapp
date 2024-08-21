package org.intelehealth.app.activities.visitSummaryActivity;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.smartcaredoc.app.R;


/**
 * Created by dell on 6/30/2017.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public ImageButton delete_btn;

    public MyViewHolder(View view)
    {
        super(view);
        imageView = view.findViewById(R.id.imageView_medical);
        delete_btn = view.findViewById(R.id.delete_btn);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ImageButton getDelete_btn() {
        return delete_btn;
    }

    public void setDelete_btn(ImageButton delete_btn) {
        this.delete_btn = delete_btn;
    }
}
