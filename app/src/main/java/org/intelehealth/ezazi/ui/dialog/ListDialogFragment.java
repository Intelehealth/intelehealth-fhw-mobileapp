package org.intelehealth.ezazi.ui.dialog;

import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.databinding.DialogListViewBinding;
import org.intelehealth.ezazi.ui.search.SearchableAdapter;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 16:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class ListDialogFragment<T> extends BaseDialogFragment<T> implements SearchView.OnQueryTextListener {

    private DialogListViewBinding listViewBinding;

    @Override
    View getContentView() {
        listViewBinding = DialogListViewBinding.inflate(getLayoutInflater(), null, false);
        listViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getAdapter() != null)
            listViewBinding.recyclerView.setAdapter(getAdapter());
        listViewBinding.searchView.setOnQueryTextListener(ListDialogFragment.this);
        return listViewBinding.getRoot();
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return null;
    }

    public void setRecyclerAdapter(RecyclerView.Adapter<?> adapter) {
        listViewBinding.recyclerView.setAdapter(adapter);
    }

    @Override
    boolean hasTitle() {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (listViewBinding.recyclerView.getAdapter() instanceof SearchableAdapter) {
            SearchableAdapter<?, RecyclerView.ViewHolder> adapter =
                    (SearchableAdapter) listViewBinding.recyclerView.getAdapter();
            adapter.getFilter().filter(newText);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}
