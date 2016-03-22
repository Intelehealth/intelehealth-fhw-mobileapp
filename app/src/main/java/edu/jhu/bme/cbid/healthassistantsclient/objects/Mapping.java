package edu.jhu.bme.cbid.healthassistantsclient.objects;

import android.support.annotation.Nullable;

/**
 * Created by tusharjois on 3/22/16.
 */
public class Mapping {
    private String id;
    private String text;
    @Nullable private String trigger; // not null = complaint
    @Nullable private String language; // not null = answer
    private String associatedCategory;
    private String associatedComplaint;
    private Mapping options;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(@Nullable String trigger) {
        this.trigger = trigger;
    }

    @Nullable
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@Nullable String language) {
        this.language = language;
    }

    public String getAssociatedCategory() {
        return associatedCategory;
    }

    public void setAssociatedCategory(String associatedCategory) {
        this.associatedCategory = associatedCategory;
    }

    public String getAssociatedComplaint() {
        return associatedComplaint;
    }

    public void setAssociatedComplaint(String associatedComplaint) {
        this.associatedComplaint = associatedComplaint;
    }

    public Mapping getOptions() {
        return options;
    }

    public void setOptions(Mapping options) {
        this.options = options;
    }
}
