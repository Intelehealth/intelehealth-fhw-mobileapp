package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class MultiChoiceAdapter<T, VH extends RecyclerView.ViewHolder> extends
        BaseSelectedRecyclerViewAdapter<T, VH> implements CompoundButton.OnCheckedChangeListener {

    public MultiChoiceAdapter(Context context, ArrayList<T> objectsList) {
        super(context, objectsList);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }
}
