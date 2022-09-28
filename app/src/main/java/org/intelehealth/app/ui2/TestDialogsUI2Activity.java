package org.intelehealth.app.ui2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.customToolip.ActionItemCustom;
import org.intelehealth.app.ui2.customToolip.QuickActionCustom;
import org.intelehealth.app.ui2.customToolip.QuickIntentActionCustom;

public class TestDialogsUI2Activity extends AppCompatActivity {
    private QuickActionCustom quickAction;
    private QuickActionCustom quickIntent;
    private static final int ID_DOWN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialogs_ui2);

        setTooltipForInternet("custom tooltip");
        ImageView ivTest = findViewById(R.id.iv_test_tooltip);


        ivTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction.show(v);
            }
        });


    }
    private void setTooltipForInternet(String message) {
        QuickActionCustom.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        QuickActionCustom.setDefaultTextColor(Color.BLACK);

        ActionItemCustom nextItem = new ActionItemCustom(ID_DOWN, message);
        quickAction = new QuickActionCustom(this, QuickActionCustom.HORIZONTAL);
        quickAction.setColorRes(R.color.white);
        quickAction.setTextColorRes(R.color.textColorBlack);
        quickAction.addActionItem(nextItem);
        quickAction.setTextColor(Color.BLACK);


        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickActionCustom.OnActionItemClickListener() {
            @Override
            public void onItemClick(ActionItemCustom item) {
                //here we can filter which action item was clicked with pos or actionId parameter
                String title = item.getTitle();
                Toast.makeText(TestDialogsUI2Activity.this, title + " selected", Toast.LENGTH_SHORT).show();
                if (!item.isSticky()) quickAction.remove(item);
            }
        });

        quickAction.setOnDismissListener(new QuickActionCustom.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Toast.makeText(HomeScreenActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        quickIntent = new QuickIntentActionCustom(this)
                .setActivityIntent(sendIntent)
                .create();
        quickIntent.setAnimStyle(QuickActionCustom.Animation.REFLECT);
    }


}