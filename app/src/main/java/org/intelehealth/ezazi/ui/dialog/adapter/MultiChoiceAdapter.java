package org.intelehealth.ezazi.ui.dialog.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class MultiChoiceAdapter<T, VH extends RecyclerView.ViewHolder> extends
        BaseSelectedRecyclerViewAdapter<T, VH> {

    public MultiChoiceAdapter(Context context, ArrayList<T> objectsList) {
        super(context, objectsList);
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view.getTag();
            int checkedPosition = (int) view.getTag(view.getId());
            if (view instanceof CompoundButton) {
                CompoundButton button = (CompoundButton) view;
                button.setChecked(!button.isChecked());
            }

            manageSelection(checkBox.isChecked(), checkedPosition);
        }
    }

    private void manageSelection(boolean isChecked, int checkedPosition) {
        if (isChecked) {
            removeSelection(getItem(checkedPosition));
        } else {
            selectItem(getItem(checkedPosition));
        }
    }
}
