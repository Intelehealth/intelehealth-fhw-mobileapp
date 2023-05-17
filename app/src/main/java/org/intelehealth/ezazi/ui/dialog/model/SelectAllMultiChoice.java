package org.intelehealth.ezazi.ui.dialog.model;

/**
 * Created by Vaghela Mithun R. on 16-05-2023 - 19:08.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SelectAllMultiChoice implements MultiChoiceItem {
    private String header = "Select All";

    @Override
    public boolean isHeader() {
        return true;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
