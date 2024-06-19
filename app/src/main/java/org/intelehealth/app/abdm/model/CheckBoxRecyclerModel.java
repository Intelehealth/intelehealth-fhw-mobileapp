package org.intelehealth.app.abdm.model;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by - Prajwal W. on 19/06/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class CheckBoxRecyclerModel {
    private String checkboxText;
    private boolean isChecked;

    public CheckBoxRecyclerModel() {
    }

    public CheckBoxRecyclerModel(String checkboxText, boolean isChecked) {
        this.checkboxText = checkboxText;
        this.isChecked = isChecked;
    }

    public String getCheckboxText() {
        return checkboxText;
    }

    public void setCheckboxText(String checkboxText) {
        this.checkboxText = checkboxText;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
