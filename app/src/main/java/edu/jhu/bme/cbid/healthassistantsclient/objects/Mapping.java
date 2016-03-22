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

}
