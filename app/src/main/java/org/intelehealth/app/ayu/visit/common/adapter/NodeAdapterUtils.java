package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.DialogUtils;

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
        Log.v(TAG, "updateForHideShowFlag - "+new Gson().toJson(toCompareWithNode));
        if (targetNode == null || toCompareWithNode == null) return;
        for (int i = 0; i < toCompareWithNode.getOptionsList().size(); i++) {
            boolean isSelected = toCompareWithNode.getOptionsList().get(i).isSelected();
            String text = toCompareWithNode.getOptionsList().get(i).getText();
            Log.v(TAG, "updateForHideShowFlag text   - "+text+" - isSelected - "+isSelected);
            for (int j = 0; j < targetNode.getOptionsList().size(); j++) {
                if (text.equals(targetNode.getOptionsList().get(j).getText())) {
                    Log.v(TAG, "updateForHideShowFlag match found!");
                    targetNode.getOptionsList().get(j).setNeedToHide(isSelected);
                }
            }
        }
    }


}
