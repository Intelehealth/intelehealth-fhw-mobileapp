package org.intelehealth.ezazi.ui.dialog.model;

import androidx.annotation.StringRes;

import java.io.Serializable;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 19:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class DialogArg<T> implements Serializable {
    private String title;
    private String positiveBtnLabel;

    private String negativeBtnLabel;

    private T content;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPositiveBtnLabel() {
        return positiveBtnLabel;
    }

    public void setPositiveBtnLabel(String positiveBtnLabel) {
        this.positiveBtnLabel = positiveBtnLabel;
    }

    public void setNegativeBtnLabel(String negativeBtnLabel) {
        this.negativeBtnLabel = negativeBtnLabel;
    }

    public String getNegativeBtnLabel() {
        return negativeBtnLabel;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
