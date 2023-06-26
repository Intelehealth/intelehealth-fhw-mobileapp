package org.intelehealth.ezazi.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogFragmentBinding;
import org.intelehealth.ezazi.ui.dialog.listener.OnDialogActionListener;
import org.intelehealth.ezazi.ui.dialog.model.DialogArg;

import java.util.Objects;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 15:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class BaseDialogFragment<T> extends AppCompatDialogFragment implements OnDialogActionListener, View.OnClickListener {
    public static final String ARGS = "dialog_args";
    protected DialogArg<T> args;

    public static Bundle getDialogArgument(DialogArg<?> arg) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS, arg);
        return bundle;
    }

    private DialogFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFragmentBinding.inflate(inflater, container, false);
        setDialogArguments();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.clDataContainer.addView(getDataContentView());
        binding.clDialogContainer.setBackground(getDialogBackground());
        binding.btnDismiss.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.tvDialogTitle.setVisibility(hasTitle() ? View.VISIBLE : View.GONE);
        super.onViewCreated(view, savedInstanceState);
    }

    public Drawable getDialogBackground() {
        return ContextCompat.getDrawable(requireContext(), R.drawable.white_child_container_bg);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void setDialogArguments() {
        if (getArguments() != null) {
            args = (DialogArg<T>) getArguments().getSerializable(ARGS);
            if (hasTitle()) binding.setTitle(args.getTitle());
            binding.setSubmitLabel(args.getPositiveBtnLabel());
            binding.setDismissLabel(args.getNegativeBtnLabel());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSubmit) {
            onSubmit();
            dismiss();
        } else if (view.getId() == R.id.btnDismiss) {
            onDismiss();
            dismiss();
        }
    }

    /**
     * Set the view layout params to fit inside the dialog view
     *
     * @return view
     */
    private View getDataContentView() {
        View view = getContentView();
        view.setLayoutParams(getConstraintLayoutParams());
        return view;
    }

    /**
     * To set container child layout params to match with container height and width
     *
     * @return LayoutParams
     */
    private LayoutParams getConstraintLayoutParams() {
        int height = getResources().getDimensionPixelOffset(R.dimen.dialog_max_height);
        int padding = getResources().getDimensionPixelOffset(R.dimen.screen_container_padding) * 5;
        LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        params.topToTop = ConstraintSet.PARENT_ID;
        params.bottomToBottom = ConstraintSet.PARENT_ID;
        params.startToStart = ConstraintSet.PARENT_ID;
        params.endToEnd = ConstraintSet.PARENT_ID;
        if (!isWrapContentDialog())
            params.matchConstraintMaxHeight = height - (binding.tvDialogTitle.getHeight() + padding);
        return params;
    }

    public void changeSubmitButtonState(boolean isActive) {
        binding.btnSubmit.setEnabled(isActive);
    }

    public boolean isWrapContentDialog() {
        return false;
    }

    /**
     * Data Container child view for dialog, it will be recycler view,
     * simple alert content. This view implemented on child class of [BaseDialogFragment]
     *
     * @return view
     */
    abstract View getContentView();

    abstract boolean hasTitle();

    public abstract static class BaseBuilder<T, D extends BaseDialogFragment<T>> {

        private final Context context;

        private String title;
        private String positiveBtnLabel;
        private String negativeBtnLabel;

        private boolean hideNegativeButton = false;

        private T content;

        private View view;

        public BaseBuilder(Context context) {
            this.context = context;
        }

        public BaseBuilder<T, D> title(@StringRes int title) {
            this.title = context.getResources().getString(title);
            return this;
        }

        public BaseBuilder<T, D> title(String title) {
            this.title = title;
            return this;
        }

        public BaseBuilder<T, D> positiveButtonLabel(@StringRes int positiveBtnLabel) {
            this.positiveBtnLabel = context.getResources().getString(positiveBtnLabel);
            return this;
        }

        public BaseBuilder<T, D> positiveButtonLabel(String positiveBtnLabel) {
            this.positiveBtnLabel = positiveBtnLabel;
            return this;
        }

        public BaseBuilder<T, D> negativeButtonLabel(@StringRes int negativeBtnLabel) {
            this.negativeBtnLabel = context.getResources().getString(negativeBtnLabel);
            return this;
        }

        public BaseBuilder<T, D> negativeButtonLabel(String negativeBtnLabel) {
            this.negativeBtnLabel = negativeBtnLabel;
            return this;
        }

        public BaseBuilder<T, D> hideNegativeButton(boolean hide) {
            this.hideNegativeButton = hide;
            return this;
        }

        public BaseBuilder<T, D> content(T content) {
            this.content = content;
            return this;
        }

        public BaseBuilder<T, D> view(View view) {
            this.view = view;
            return this;
        }

        public abstract D build();

        protected Bundle bundle() {
            DialogArg<T> args = new DialogArg<>();
            args.setTitle(title);
            args.setPositiveBtnLabel(positiveBtnLabel);
            args.setNegativeBtnLabel(negativeBtnLabel == null ? context.getResources().getString(R.string.cancel) : negativeBtnLabel);
            if (hideNegativeButton) args.setNegativeBtnLabel(null);
            args.setContent(content);
            return getDialogArgument(args);
        }

        protected View getView() {
            return view;
        }
    }
}
