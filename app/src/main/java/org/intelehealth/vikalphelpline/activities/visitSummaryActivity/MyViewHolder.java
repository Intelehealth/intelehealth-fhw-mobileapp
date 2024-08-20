package org.intelehealth.vikalphelpline.activities.visitSummaryActivity;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.intelehealth.vikalphelpline.R;


/**
 * Created by dell on 6/30/2017.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;

    public MyViewHolder(View view)
    {
        super(view);
        imageView = view.findViewById(R.id.imageView_medical);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
