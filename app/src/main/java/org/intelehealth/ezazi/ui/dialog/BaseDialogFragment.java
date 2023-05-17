package org.intelehealth.ezazi.ui.dialog;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
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

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogFragmentBinding;
import org.intelehealth.ezazi.ui.dialog.listener.OnDialogActionListener;
import org.intelehealth.ezazi.ui.dialog.model.DialogArg;

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
        binding.btnDismiss.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.tvDialogTitle.setVisibility(hasTitle() ? View.VISIBLE : View.GONE);
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color
                .TRANSPARENT));
        return dialog;
    }

    private void setDialogArguments() {
        if (getArguments() != null) {
            args = (DialogArg<T>) getArguments().getSerializable(ARGS);
            if (hasTitle()) binding.setTitle(args.getTitle());
            binding.setSubmitLabel(args.getPositiveBtnLabel());
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

    private LayoutParams getConstraintLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topToTop = ConstraintSet.PARENT_ID;
        params.bottomToBottom = ConstraintSet.PARENT_ID;
        params.startToStart = ConstraintSet.PARENT_ID;
        params.endToEnd = ConstraintSet.PARENT_ID;
        return params;
    }

    public void changeSubmitButtonState(boolean isActive) {
        binding.btnSubmit.setEnabled(isActive);
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
        private int title;
        private int positiveBtnLabel;
        private int negativeBtnLabel;

        private T content;

        public BaseBuilder<T, D> title(@StringRes int title) {
            this.title = title;
            return this;
        }

        public BaseBuilder<T, D> positiveButtonLabel(@StringRes int positiveBtnLabel) {
            this.positiveBtnLabel = positiveBtnLabel;
            return this;
        }

        public BaseBuilder<T, D> negativeButtonLabel(@StringRes int negativeBtnLabel) {
            this.negativeBtnLabel = negativeBtnLabel;
            return this;
        }

        public BaseBuilder<T, D> content(T content) {
            this.content = content;
            return this;
        }

        public abstract D build();

        protected Bundle bundle() {
            DialogArg<T> args = new DialogArg<>();
            args.setTitle(title);
            args.setPositiveBtnLabel(positiveBtnLabel);
            args.setNegativeBtnLabel(negativeBtnLabel);
            args.setContent(content);
            return getDialogArgument(args);
        }
    }
}
