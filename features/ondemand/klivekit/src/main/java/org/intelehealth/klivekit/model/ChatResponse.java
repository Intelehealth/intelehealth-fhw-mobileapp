package org.intelehealth.klivekit.model;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun R. on 24-07-2023 - 23:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ChatResponse {
    private boolean success;
    private ArrayList<ChatMessage> data;

    public ArrayList<ChatMessage> getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }
}
