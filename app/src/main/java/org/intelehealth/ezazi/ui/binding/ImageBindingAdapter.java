package org.intelehealth.ezazi.ui.binding;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.ezazi.R;

import java.io.File;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 23:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ImageBindingAdapter {
    @BindingAdapter({"imgUrl", "imgContent"})
    public static void bindImage(ImageView imageView, String imgUrl, String imgContent) {
        String latter = imgContent.substring(0, 1);
        Resources resources = imageView.getContext().getResources();
        int textSize = resources.getDimensionPixelOffset(R.dimen.std_30sp);
        Bitmap textBitmap = textAsBitmap(latter, textSize, Color.BLACK, imageView.getHeight());
        if (imgUrl != null && !imgUrl.isEmpty()) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(imageView.getContext()).asDrawable().sizeMultiplier(0.25f);
            Glide.with(imageView.getContext())
                    .load(new File(imgUrl))
                    .thumbnail(requestBuilder)
                    .centerCrop()
                    .error(textBitmap)
                    .placeholder(new BitmapDrawable(resources, textBitmap))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imageView);
        } else imageView.setImageBitmap(textBitmap);
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor, int size) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
//        float baseline = -paint.ascent(); // ascent() is negative
//        int width = (int) (paint.measureText(text) + 0.5f); // round
//        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Rect boundsText = new Rect();
        paint.getTextBounds(text, 0, text.length(), boundsText);

        int x = (bitmap.getWidth()) / 2;
        int y = (bitmap.getHeight() + boundsText.height()) / 2;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);
        return bitmap;
    }
}
