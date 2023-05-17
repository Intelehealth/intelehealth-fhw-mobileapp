package org.intelehealth.ezazi.ui.dialog.model;

import androidx.annotation.StringRes;

import java.io.Serializable;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 19:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class DialogArg<T> implements Serializable {
    @StringRes
    private int title;
    @StringRes
    private int positiveBtnLabel;

    @StringRes
    private int negativeBtnLabel;

    private T content;


    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getPositiveBtnLabel() {
        return positiveBtnLabel;
    }

    public void setPositiveBtnLabel(int positiveBtnLabel) {
        this.positiveBtnLabel = positiveBtnLabel;
    }

    public void setNegativeBtnLabel(int negativeBtnLabel) {
        this.negativeBtnLabel = negativeBtnLabel;
    }

    public int getNegativeBtnLabel() {
        return negativeBtnLabel;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
