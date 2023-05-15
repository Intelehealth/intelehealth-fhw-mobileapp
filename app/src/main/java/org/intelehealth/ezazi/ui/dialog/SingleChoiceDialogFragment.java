package org.intelehealth.ezazi.ui.dialog;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingleChoiceDialogFragment extends ListDialogFragment {
    private List<String> choices;

    public SingleChoiceDialogFragment(List<String> choices) {
        this.choices = choices;
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
