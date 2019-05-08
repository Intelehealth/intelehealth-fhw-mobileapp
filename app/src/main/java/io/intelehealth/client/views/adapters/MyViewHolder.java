package io.intelehealth.client.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import io.intelehealth.client.R;

/**
 * Created by dell on 6/30/2017.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;

    public MyViewHolder(View view)
    {
        super(view);
        imageView=(ImageView)view.findViewById(R.id.imageView_medical);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
