package org.intelehealth.ezazi.ui.dialog;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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

    private final OnChoiceListener listener;
    private SingleChoiceAdapter adapter;

    public SingleChoiceDialogFragment(OnChoiceListener listener) {
        this.listener = listener;
    }

    @Override
    RecyclerView.Adapter<?> getAdapter() {
        adapter = new SingleChoiceAdapter(getContext(), args.getContent(), this);
        return adapter;
    }

    @Override
    public void onSubmit() {
        listener.onItemSelected(adapter.getSelected(), args.getContent().get(adapter.getSelected()));
    }

    @Override
    public void onClick(View view) {
        int previousSelection = adapter.getSelected();
        int selected = (int) view.getTag();
        if (previousSelection == selected) selected = -1;
        adapter.setSelected(selected);
        changeSubmitButtonState(adapter.getSelected() == -1);
    }
}
