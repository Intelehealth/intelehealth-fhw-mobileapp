package org.intelehealth.app.ui.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.intelehealth.app.models.IntroContent

/**
 * Created by Vaghela Mithun R. on 25-04-2024 - 11:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

@BindingAdapter("content")
fun bindContentImage(imageView: ImageView?, content: IntroContent?) {
    if (imageView != null && content != null) {
        Glide.with(imageView.context)
            .load(if (content.isResourceImage) content.resId else content.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView);
    }
}