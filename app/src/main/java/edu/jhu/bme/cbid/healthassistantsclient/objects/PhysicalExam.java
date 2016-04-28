package edu.jhu.bme.cbid.healthassistantsclient.objects;

import org.json.JSONObject;

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class PhysicalExam extends Node {

    private String selection;

    public PhysicalExam (JSONObject jsonObject, String selection){
        super(jsonObject);
        this.selection = selection;
    }

    


}
