package org.intelehealth.ezazi.ui.dialog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.ui.search.SearchableAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 10:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@SuppressLint("NotifyDataSetChanged")
public abstract class BaseSelectedRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder>
        extends SearchableAdapter<T, VH> implements View.OnClickListener {
    private final ArrayList<T> selectedItems = new ArrayList<>();
    private List<T> items;
    protected Context context;
    protected LayoutInflater inflater;

    @Override
    public int getItemCount() {
        return items.size();
    }

    public BaseSelectedRecyclerViewAdapter(Context context, List<T> objectsList) {
        super(objectsList);
        inflater = LayoutInflater.from(context);
        items = objectsList;
        setHasStableIds(true);
    }

    public T getItem(int position) {
        return items.get(position);
    }

    public List<T> getItems() {
        return items;
    }

    public void toggleSelection(int position) {
        T item = getItem(position);
        toggleSelection(item);
    }

    public void toggleSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyItem(item);
    }

    public void removeSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        }
        notifyItem(item);
    }

    public void selectItem(int position) {
        T item = getItem(position);
        selectItem(item);
    }

    public void selectItem(T item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyItem(item);
    }

    private void notifyItem(T item) {
        notifyItemChanged(getItems().indexOf(item));
    }


    public void selectAllItem() {
        selectedItems.clear();
        selectedItems.addAll(getItems());
        notifyDataSetChanged();
    }

    public void updateItems(ArrayList<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public Collection<T> getSelectedItems() {
        return selectedItems;
    }

    public boolean isItemSelected(int position) {
        return selectedItems.size() > 0 && selectedItems.contains(getItem(position));
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    protected void onResultSearch(ArrayList<T> results) {
        updateItems(results);
    }
}
