package io.intelehealth.client.instruction;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.widget.Spinner;

import com.azimolabs.conditionwatcher.Instruction;

import io.intelehealth.client.R;
import io.intelehealth.client.application.IntelehealthApplication;

/**
 * Created by Dexter Barretto on 8/12/17.
 * Github : @dbarretto
 */

public class ServerListLoadingInstruction extends Instruction {

    @Override
    public String getDescription() {
        return "Spinner must not be null";
    }

    @Override
    public boolean checkCondition() {
        Activity activity = ((IntelehealthApplication)
                InstrumentationRegistry.getTargetContext().getApplicationContext()).getCurrentActivity();
        if (activity == null) return false;

        Spinner srItemList = (Spinner) activity.findViewById(R.id.spinner_location);
        return true;
    }
}
