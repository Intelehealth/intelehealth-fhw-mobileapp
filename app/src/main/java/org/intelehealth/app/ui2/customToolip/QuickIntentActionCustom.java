package org.intelehealth.app.ui2.customToolip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;


public class QuickIntentActionCustom {
    private static final int SERVICE = 1;
    private static final int ACTIVITY = 0;

    private Context mContext;
    private Intent mIntent;
    private int mOrientation;
    private int mIntentType = ACTIVITY;
    private QuickActionCustom.OnActionItemClickListener mOnActionItemClick;
    private String mType[] = {"Activity", "Service"};

    /**
     * Constructor for default vertical layout
     *
     * @param context require
     */
    public QuickIntentActionCustom(Context context) {
        this(context, QuickActionCustom.VERTICAL);
    }

    public QuickIntentActionCustom(Context context, int orientation) {
        mContext = context;
        mOrientation = orientation;
    }

    public QuickIntentActionCustom setServiceIntent(Intent services) {
        mIntent = services;
        mIntentType = SERVICE;
        return this;
    }

    public QuickIntentActionCustom setActivityIntent(Intent activity) {
        mIntent = activity;
        mIntentType = ACTIVITY;
        return this;
    }

    public QuickIntentActionCustom serOnActionItemClickListener(QuickActionCustom.OnActionItemClickListener onClick) {
        mOnActionItemClick = onClick;
        return this;
    }

    public QuickActionCustom create() {
        if (mIntent == null)
            throw new IllegalStateException(
                    "Must set intent be for create(), Use setActivityIntent() or "+"setServiceIntent()");

        QuickActionCustom quickAction = new QuickActionCustom(mContext, mOrientation);
        // Add List of Support Activity or Services
        if (mIntent != null) {
            final List<ResolveInfo> lists;
            PackageManager pm = mContext.getPackageManager();

            switch (mIntentType) {
                case SERVICE:
                    lists = pm.queryIntentServices(mIntent, 0);
                    break;
                case ACTIVITY:
                default:
                    lists = pm.queryIntentActivities(mIntent, 0);
                    break;
            }
            // Add Action Item of support intent.
            if (lists.size() > 0) {
                int index = 0;
                for (ResolveInfo info : lists) {
                    ActionItemCustom item = new ActionItemCustom(index++, (String)info.loadLabel(pm));
                    item.setIconDrawable(info.loadIcon(pm));
                    quickAction.addActionItem(item);
                }
                addOnActionItemClick(quickAction, lists);
            } else {
                ActionItemCustom item = new ActionItemCustom(0, "Not found support any"+mType[mIntentType]+"!");
                quickAction.addActionItem(item);
            }
        }
        return quickAction;
    }

    private void addOnActionItemClick(QuickActionCustom action, final List<ResolveInfo> lists) {
        // If not explicit add then we'll Add Default OnActionItemClick
        if (mOnActionItemClick != null)
            action.setOnActionItemClickListener(mOnActionItemClick);
        else {
            setDefaultOnActionItemClick(action, lists);
        }
    }

    private void setDefaultOnActionItemClick(QuickActionCustom action, final List<ResolveInfo> lists) {
        switch (mIntentType) {
            case SERVICE:
                action.setOnActionItemClickListener(new QuickActionCustom.OnActionItemClickListener() {
                    @Override public void onItemClick(ActionItemCustom item) {
                        ResolveInfo info = lists.get(item.getActionId());
                        String name = info.serviceInfo.name;
                        String packageName = info.serviceInfo.packageName;

                        Intent service = new Intent(mIntent);
                        service.setComponent(new ComponentName(packageName, name));
                        mContext.startService(service);
                    }
                });
                break;
            case ACTIVITY:
            default:
                action.setOnActionItemClickListener(new QuickActionCustom.OnActionItemClickListener() {
                    @Override public void onItemClick(ActionItemCustom item) {
                        ResolveInfo info = lists.get(item.getActionId());
                        String name = info.activityInfo.name;
                        String packageName = info.activityInfo.packageName;

                        Intent intent = new Intent(mIntent);
                        intent.setComponent(new ComponentName(packageName, name));
                        mContext.startActivity(intent);
                    }
                });
                break;
        }
    }
}
