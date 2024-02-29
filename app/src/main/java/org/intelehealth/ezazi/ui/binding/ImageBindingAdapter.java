package org.intelehealth.ezazi.ui.binding;

import android.graphics.drawable.Drawable;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.ezazi.ui.custom.TextDrawable;

import java.io.File;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 23:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ImageBindingAdapter {
    @BindingAdapter({"imgUrl", "imgContent"})
    public static void bindImage(ImageView imageView, String imgUrl, String imgContent) {
//        if (imgUrl != null && !imgUrl.isEmpty()) {
        Drawable textDrawable = new TextDrawable(imgContent.substring(0, 1).toUpperCase());
        RequestBuilder<Drawable> requestBuilder = Glide.with(imageView.getContext()).asDrawable().sizeMultiplier(0.25f);
        Glide.with(imageView.getContext())
                .load(new File(imgUrl))
                .thumbnail(requestBuilder)
                .centerCrop()
                .error(textDrawable)
                .placeholder(textDrawable)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView);
//        }
    }
}
