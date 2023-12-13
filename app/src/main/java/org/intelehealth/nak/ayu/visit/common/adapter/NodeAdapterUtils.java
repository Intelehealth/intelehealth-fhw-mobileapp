package org.intelehealth.nak.ayu.visit.common.adapter;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.intelehealth.nak.R;
import org.intelehealth.nak.app.IntelehealthApplication;
import org.intelehealth.nak.ayu.visit.model.ReasonData;
import org.intelehealth.nak.knowledgeEngine.Node;
import org.intelehealth.nak.utilities.DialogUtils;
import org.intelehealth.nak.utilities.FileUtils;
import org.intelehealth.nak.utilities.SessionManager;
import org.json.JSONObject;

public class NodeAdapterUtils {
    public static final String TAG = NodeAdapterUtils.class.getSimpleName();

    /**
     * @param context
     * @param title
     * @param message
     */
    public static void showKnowMoreDialog(Context context, String title, String message) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(context, 0, title, message, true, context.getResources().getString(R.string.okay), context.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

    /**
     * @param context
     * @param targetNode
     * @param toCompareWithNode
     */
    public static void updateForHideShowFlag(Context context, Node targetNode, Node toCompareWithNode) {
        Log.v(TAG, "updateForHideShowFlag - " + new Gson().toJson(toCompareWithNode));
        if (targetNode == null || toCompareWithNode == null) return;
        for (int i = 0; i < toCompareWithNode.getOptionsList().size(); i++) {
            boolean isSelected = toCompareWithNode.getOptionsList().get(i).isSelected();
            String text = toCompareWithNode.getOptionsList().get(i).getText();
            Log.v(TAG, "updateForHideShowFlag text   - " + text + " - isSelected - " + isSelected);
            for (int j = 0; j < targetNode.getOptionsList().size(); j++) {
                if (text.equals(targetNode.getOptionsList().get(j).getText())) {
                    Log.v(TAG, "updateForHideShowFlag match found!");
                    targetNode.getOptionsList().get(j).setNeedToHide(isSelected);
                }
            }
        }
    }

    public static String getTheChiefComplainNameWRTLocale(Context context, String chiefComplainName) {
        String fileLocation = "engines/" + chiefComplainName + ".json";
        JSONObject currentFile = FileUtils.encodeJSON(context, fileLocation);
        Node mainNode = new Node(currentFile);
        return mainNode.findDisplay();
    }


    public static char getStartCharAsPerLocale() {
        char result = 'A';
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String locale = sessionManager.getCurrentLang();

        switch (locale) {

            case "gu":
                break;
            case "bn":
                break;
            case "ta":
                break;
            case "or":
                result = 'ଅ';
                break;
            case "hi":
                result = 'अ';
                break;
            case "te":
                break;
            case "mr":
                break;
            case "as":
                break;
            case "ml":
                break;
            case "kn":
                break;

        }

        return result;
    }

    public static char getEndCharAsPerLocale() {
        char result = 'Z';
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String locale = sessionManager.getCurrentLang();

        switch (locale) {

            case "gu":
                break;
            case "bn":
                break;
            case "ta":
                break;
            case "or":
                result = 'ୱ';
                break;
            case "hi":
                result = 'ह';
                break;
            case "te":
                break;
            case "mr":
                break;
            case "as":
                break;
            case "ml":
                break;
            case "kn":
                break;

        }

        return result;
    }

    public static String formatChiefComplainWithLocaleName(ReasonData reasonData) {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String locale = sessionManager.getCurrentLang();
        if (!locale.equalsIgnoreCase("en")) {
            return reasonData.getReasonName() + " [ " + reasonData.getReasonNameLocalized() + " ] ";
        } else {
            return reasonData.getReasonName();
        }
    }

    public static String getEngChiefComplainNameOnly(String item) {
        if (item.contains("[")) {
            return item.split("\\[")[0].trim();
        } else {
            return item;
        }
    }
}
