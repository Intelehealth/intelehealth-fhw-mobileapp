package org.intelehealth.ezazi.ui.search;

import static java.sql.DriverManager.println;

import android.content.Context;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.ui.dialog.adapter.BaseSelectedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vaghela Mithun R. on 20-07-2023 - 11:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class SearchableAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Filterable {

    private static final String TAG = "SearchableAdapter";

    protected ArrayList<T> searchableList = new ArrayList<>();

    public SearchableAdapter(List<T> items) {
        searchableList.addAll(items);
    }

    public void updateSearchable(ArrayList<T> newData) {
        searchableList = new ArrayList<T>(newData);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                FilterResults results = new FilterResults();
                if (query == null || query.length() == 0) {
                    results.values = searchableList;
                    results.count = searchableList.size();
                } else {
                    ArrayList<T> filteredList = new ArrayList<T>();
                    for (int position = 0; position < searchableList.size(); position++) {
                        String value = searchableValue(position).toLowerCase(Locale.getDefault()).trim();
                        String lowerQuery = query.toString().toLowerCase(Locale.getDefault()).trim();
                        if (value.contains(lowerQuery)) {
                            filteredList.add(searchableList.get(position));
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0)
                    onResultSearch((ArrayList<T>) results.values);
                else
                    onResultSearch(new ArrayList<>());
            }
        };
    }

    protected abstract String searchableValue(int position);

    protected abstract void onResultSearch(ArrayList<T> results);
}
