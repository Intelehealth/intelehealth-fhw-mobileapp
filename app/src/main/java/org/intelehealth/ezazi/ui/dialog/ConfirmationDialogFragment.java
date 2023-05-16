package org.intelehealth.ezazi.ui.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.DialogConfirmationViewBinding;
import org.intelehealth.ezazi.databinding.DialogListViewBinding;
import org.intelehealth.ezazi.ui.dialog.model.DialogArg;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ConfirmationDialogFragment extends BaseDialogFragment<String> implements View.OnClickListener {
    private OnConfirmationActionListener listener;

    public static ConfirmationDialogFragment getInstance(DialogArg<?> arg) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle bundle = getDialogArgument(arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnConfirmationActionListener {
        void onAccept();

        default void onDecline() {
        }
    }

    public void setListener(OnConfirmationActionListener listener) {
        this.listener = listener;
    }

    @Override
    View getContentView() {
        DialogConfirmationViewBinding binding = DialogConfirmationViewBinding.inflate(getLayoutInflater(), null, false);
        binding.setContent(args.getContent());
        binding.setTitle(args.getTitle());
        return binding.getRoot();
    }

    @Override
    boolean hasTitle() {
        return false;
    }

    @Override
    public void onSubmit() {
        if (listener != null) listener.onAccept();
    }

    @Override
    public void onDismiss() {
        if (listener != null) listener.onDecline();
    }
}
