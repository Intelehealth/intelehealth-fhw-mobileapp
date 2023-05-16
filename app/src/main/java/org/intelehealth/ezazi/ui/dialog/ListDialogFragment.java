package org.intelehealth.ezazi.ui.dialog;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.DialogListViewBinding;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class ListDialogFragment<T> extends BaseDialogFragment<T> implements View.OnClickListener {

    @Override
    View getContentView() {
        DialogListViewBinding listViewBinding = DialogListViewBinding.inflate(getLayoutInflater(), null, false);
        listViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listViewBinding.recyclerView.setAdapter(getAdapter());
        return listViewBinding.getRoot();
    }

    abstract RecyclerView.Adapter<?> getAdapter();

    @Override
    boolean hasTitle() {
        return true;
    }
}
