package org.intelehealth.unicef.ayu.visit.common.adapter;

import android.content.Context;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.utilities.DialogUtils;

public class NodeAdapterUtils {
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


}
