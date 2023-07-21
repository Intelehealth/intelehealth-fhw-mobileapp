package org.intelehealth.ezazi.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.SingleChoiceAdapter;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceDialogFragment extends ListDialogFragment<List<SingChoiceItem>> {

    public interface OnChoiceListener {
        void onItemSelected(SingChoiceItem item);
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
        if (adapter.getSelectedItems().size() > 0) {
            listener.onItemSelected(new ArrayList<>(adapter.getSelectedItems()).get(0));
        } else {
            Toast.makeText(getContext(), "Please select item", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvChoice) {
            SingChoiceItem selected = (SingChoiceItem) view.getTag();
            int position = (int) view.getTag(R.id.tvChoice);

            if (adapter.isItemSelected(position)) {
                adapter.removeSelection(selected);
            } else {
                adapter.clearSelection();
                adapter.selectItem(selected);
            }

            changeSubmitButtonState(adapter.getSelectedItems().size() > 0);

            if (args.getPositiveBtnLabel() == null && adapter.getSelectedItems().size() > 0) {
                Log.e("Dialog", "onClick: ");
                listener.onItemSelected(new ArrayList<>(adapter.getSelectedItems()).get(0));
                dismiss();
            }
        } else super.onClick(view);
    }

    public static class Builder extends BaseBuilder<List<SingChoiceItem>, SingleChoiceDialogFragment> {

        private OnChoiceListener listener;

        public Builder(Context context) {
            super(context);
        }

        public Builder listener(OnChoiceListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public SingleChoiceDialogFragment build() {
            SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
            fragment.setArguments(bundle());
            fragment.setListener(listener);
            return fragment;
        }
    }
}
