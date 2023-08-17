package org.intelehealth.ezazi.ui.dialog.model;

/**
 * Created by Vaghela Mithun R. on 22-07-2023 - 00:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class SingChoiceItem {
    private int itemIndex;
    private String item;
    private String itemId;

    private boolean selected;

    private String secondaryName;

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSecondaryName(String secondaryName) {
        this.secondaryName = secondaryName;
    }

    public String getSecondaryName() {
        return secondaryName;
    }
}
