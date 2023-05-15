package org.intelehealth.ezazi.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogFragmentBinding;
import org.intelehealth.ezazi.ui.dialog.listener.OnDialogActionListener;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 15:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class BaseDialogFragment extends AppCompatDialogFragment implements OnDialogActionListener, View.OnClickListener {
    private DialogFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.clDataContainer.addView(getDataContentView());
        binding.btnDismiss.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSubmit) {
            onSubmit();
        } else if (view.getId() == R.id.btnDismiss) {
            onDismiss();
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

    /**
     * Data Container child view for dialog, it will be recycler view,
     * simple alert content. This view implemented on child class of [BaseDialogFragment]
     *
     * @return view
     */
    abstract View getContentView();
}
