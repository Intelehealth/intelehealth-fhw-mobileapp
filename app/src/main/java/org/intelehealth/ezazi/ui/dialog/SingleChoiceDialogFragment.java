package org.intelehealth.ezazi.ui.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.SingleChoiceAdapter;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceDialogFragment extends ListDialogFragment<List<String>> {

    public interface OnChoiceListener {
        void onItemSelected(int position, String value);
    }

    private OnChoiceListener listener;
    private SingleChoiceAdapter adapter;

    public void setListener(OnChoiceListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.Adapter<?> getAdapter() {
        adapter = new SingleChoiceAdapter(getContext(), args.getContent(), this);
        return adapter;
    }

    @Override
    public void onSubmit() {
        listener.onItemSelected(adapter.getSelected(), args.getContent().get(adapter.getSelected()));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvChoice) {
            int previousSelection = adapter.getSelected();
            int selected = (int) view.getTag();
            if (previousSelection == selected) selected = -1;
            adapter.setSelected(selected);
            changeSubmitButtonState(adapter.getSelected() != -1);
        } else super.onClick(view);
    }

    public static final class Builder extends BaseBuilder<List<String>, SingleChoiceDialogFragment> {
        @Override
        public SingleChoiceDialogFragment build() {
            SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
            fragment.setArguments(bundle());
            return fragment;
        }
    }
}
