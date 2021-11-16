package org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner;

public class Item {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    private Boolean value;

    public Item(String name, Boolean value) {
        this.name = name;
        this.value = value;
    }
}
