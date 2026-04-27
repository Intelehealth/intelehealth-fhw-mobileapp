package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import org.intelehealth.app.utilities.CustomLog;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONObject;

import java.util.Locale;

public class NodeAdapterUtils {
    public static final String TAG = NodeAdapterUtils.class.getSimpleName();

    /**
     * Whether selecting an options chip should open the digital stethoscope flow.
     * Matches {@code QuestionsAdapter} behaviour: {@code input-type} {@code ayu_device} (any common spelling),
     * or {@code Heart_Sound} / {@code Lung_Sound} appearing on parent+chip labels or on the chip alone.
     */
    public static boolean shouldOpenDigitalStethoscopeFromChip(Node parentQuestion, Node chipNode) {
        if (chipNode == null || parentQuestion == null || !chipNode.isSelected()) {
            return false;
        }
        String inputType = chipNode.getInputType();
        if (inputType != null) {
            String normalized = inputType.trim().toLowerCase(Locale.ROOT).replace('-', '_');
            if ("ayu_device".equals(normalized) || normalized.contains("ayu_device")) {
                return true;
            }
        }
        String parentText = lower(parentQuestion.getText());
        String parentDisplay = lower(parentQuestion.findDisplay());
        String chipText = lower(chipNode.getText());
        String chipDisplay = lower(chipNode.findDisplay());
        String[] candidates = new String[]{
                parentText + ": " + chipDisplay,
                parentDisplay + ": " + chipDisplay,
                parentText + ": " + chipText,
                parentDisplay + ": " + chipText,
                chipText,
                chipDisplay
        };
        for (String s : candidates) {
            if (s.contains("heart_sound") || s.contains("lung_sound")) {
                return true;
            }
        }
        return false;
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

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
        CustomLog.v(TAG, "updateForHideShowFlag - " + new Gson().toJson(toCompareWithNode));
        if (targetNode == null || toCompareWithNode == null) return;
        for (int i = 0; i < toCompareWithNode.getOptionsList().size(); i++) {
            boolean isSelected = toCompareWithNode.getOptionsList().get(i).isSelected();
            String text = toCompareWithNode.getOptionsList().get(i).getText();
            CustomLog.v(TAG, "updateForHideShowFlag text   - " + text + " - isSelected - " + isSelected);
            for (int j = 0; j < targetNode.getOptionsList().size(); j++) {
                if (text.equals(targetNode.getOptionsList().get(j).getText())) {
                    CustomLog.v(TAG, "updateForHideShowFlag match found!");
                    targetNode.getOptionsList().get(j).setNeedToHide(isSelected);
                }
            }
        }
    }

    public static String getTheChiefComplainNameWRTLocale(Context context, String chiefComplainName) {

        JSONObject currentFile = null;
        if (!new SessionManager(context).getLicenseKey().isEmpty()) {
            currentFile = FileUtils.encodeJSONFromFile(context, chiefComplainName + ".json");
        }else{
            String fileLocation = "engines/" + chiefComplainName + ".json";
            currentFile = FileUtils.encodeJSON(context, fileLocation);
        }
        //JSONObject currentFile = FileUtils.encodeJSON(context, fileLocation);
        if (currentFile != null) {
            Node mainNode = new Node(currentFile);
            return mainNode.findDisplay();
        } else return "";
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
            case "hi", "mr":
                result = 'अ';
                break;
            case "te":
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
            case "hi", "mr":
                result = 'ह';
                break;
            case "te":
                break;
            //case "mr":
             //   break;
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
