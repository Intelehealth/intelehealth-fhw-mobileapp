package io.intelehealth.client.views.activites;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.intelehealth.client.R;
import io.intelehealth.client.databinding.ActivitySetupBinding;
import io.intelehealth.client.databinding.ContentSetupBinding;
import io.intelehealth.client.viewModels.SetupViewModel;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    MyClickHandlers handlers = new MyClickHandlers(this);
    SetupViewModel setupViewModel;
    ActivitySetupBinding activitySetupBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setup);
        activitySetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        ContentSetupBinding binding = DataBindingUtil.setContentView(this, R.layout.content_setup);
        setupViewModel = ViewModelProviders.of(this).get(SetupViewModel.class);
        /*set handlers with data binding*/
        activitySetupBinding.setHandlers(handlers);
        activitySetupBinding.setViewmodel(setupViewModel);
        activitySetupBinding.setLifecycleOwner(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    private class MyClickHandlers extends SetupActivity {

        public MyClickHandlers(SetupActivity setupActivity) {
        }

    }
}
