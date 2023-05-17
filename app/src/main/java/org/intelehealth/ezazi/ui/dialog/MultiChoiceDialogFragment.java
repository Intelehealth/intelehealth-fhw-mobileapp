package org.intelehealth.ezazi.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.ui.dialog.adapter.MultiChoiceAdapter;
import org.intelehealth.ezazi.ui.dialog.model.MultiChoiceItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class MultiChoiceDialogFragment<T> extends ListDialogFragment<List<T>> {

    public interface OnChoiceListener<T> {
        void onItemSelected(List<T> selectedItems);
    }

    private OnChoiceListener<T> listener;
    private MultiChoiceAdapter<T, RecyclerView.ViewHolder> adapter;

    @Override
    public void onSubmit() {
        if (listener != null) {
            listener.onItemSelected((List<T>) adapter.getSelectedItems());
        }
    }

    public void setAdapter(MultiChoiceAdapter<T, RecyclerView.ViewHolder> adapter) {
        this.adapter = adapter;
    }

    public void setListener(OnChoiceListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecyclerAdapter(adapter);
    }

    public static final class Builder<T> extends BaseBuilder<List<T>, MultiChoiceDialogFragment<T>> {

        @Override
        public MultiChoiceDialogFragment<T> build() {
            MultiChoiceDialogFragment<T> fragment = new MultiChoiceDialogFragment<>();
            fragment.setArguments(bundle());
            return fragment;
        }
    }
}
