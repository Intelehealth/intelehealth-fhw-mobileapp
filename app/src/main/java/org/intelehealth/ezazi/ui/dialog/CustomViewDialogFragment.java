package org.intelehealth.ezazi.ui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogConfirmationViewBinding;
import org.intelehealth.ezazi.ui.dialog.model.DialogArg;

import java.util.Objects;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CustomViewDialogFragment extends BaseDialogFragment<String> implements View.OnClickListener {
    private OnConfirmationActionListener listener;
    private View view;

    public interface OnConfirmationActionListener {
        void onAccept();

        default void onDecline() {
        }
    }

    public void setListener(OnConfirmationActionListener listener) {
        this.listener = listener;
    }

    public void setCustomView(View view) {
        this.view = view;
    }

    @Override
    View getContentView() {
        return view;
    }

    @Override
    public Drawable getDialogBackground() {
        return ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.container_bg_rounded);
    }

    @Override
    boolean hasTitle() {
        return true;
    }

    @Override
    public void onSubmit() {
        if (listener != null) listener.onAccept();
    }

    @Override
    public void onDismiss() {
        if (listener != null) listener.onDecline();
    }

    public static final class Builder extends BaseBuilder<String, CustomViewDialogFragment> {

        public Builder(Context context) {
            super(context);
        }

        @Override
        public CustomViewDialogFragment build() {
            CustomViewDialogFragment fragment = new CustomViewDialogFragment();
            fragment.setCustomView(getView());
            fragment.setArguments(bundle());
            return fragment;
        }
    }
}
