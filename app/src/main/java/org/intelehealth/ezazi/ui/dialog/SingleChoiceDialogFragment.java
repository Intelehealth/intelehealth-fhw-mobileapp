package org.intelehealth.ezazi.ui.dialog;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceDialogFragment<T> extends ListDialogFragment {
    @Override
    View getContentView() {
        return null;
    }

    @Override
    <VH extends RecyclerView.ViewHolder> RecyclerView.Adapter<VH> getAdapter() {
        return null;
    }

    @Override
    public void onSubmit() {

    }

    @Override
    public void onDismiss() {

    }
}
